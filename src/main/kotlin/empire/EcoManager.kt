package empire
import creep.CreepRole
import creep.CreepTask
import creep.CreepType
import creep.eco.EcoCreep
import creep.eco.WorkerCreep
import creep.getTask
import empire.eco.buildBody
import job.Job
import job.JobType
import log.LogLevel
import log.log
import memory.*
import room.getJobRequireingCreeps
import screeps.api.*
import screeps.api.structures.StructureExtension
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject

class EcoManager {
    private val ecoCreeps = mutableListOf<EcoCreep>()
    private val ecoCreepsIdle = mutableListOf<Creep>()
    fun run() {
        //delete(Memory.rooms[room.name])
        initializeWorkerCreeps()
        processIdleCreeps()
        processCreeps()

        //Process Rooms
        for ((_, room) in Game.rooms) {
            processRoom(room)

            if (Game.time >= room.memory.planNextCheck ) {
                val bm = BaseBuilder()
                bm.planMaker(room)
            }

        }
    }

    /*
    * Assign Creep to queues
    * */
    fun initializeWorkerCreeps() {
        for (creep in Game.creeps.values) {
            if (creep.spawning) { continue }
            if (creep.memory.type == "") { creep.suicide() }
            if (creep.memory.type !== CreepType.ECO.name) { continue }

            when (creep.memory.role) {
                CreepRole.WORKER.name -> ecoCreeps.add(WorkerCreep(creep))
            }

            if (creep.getTask() == CreepTask.IDLE) {
                ecoCreepsIdle.add(creep)
            }

        }
    }
    fun processIdleCreeps() {
        if (ecoCreepsIdle.isEmpty()) { return }
        log(LogLevel.DEBUG,"Processing ${ecoCreepsIdle.size} idle creep","processIdleCreeps","")

        var jobs = mutableListOf<Job>()
        //Look for jobs
        var room: Room = ecoCreepsIdle[0].room

        // find source jobs
        room.find(FIND_SOURCES).forEach {
            if (it.energy > 0) {
                jobs.add(
                    Job.create(it.id,
                        it.pos,
                        JobType.HARVEST_SOURCE
                        )
                )
            }
        }

        room.find(FIND_MY_SPAWNS).forEach {
            if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                jobs.add(
                    Job.createDropOffJob(it.id,
                        it.pos,
                        RESOURCE_ENERGY,
                        it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                        JobType.DROP_OFF_ENERGY
                    )
                )
            }
        }

        room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_EXTENSION }
            .unsafeCast<List<StructureExtension>>().forEach {
            if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                jobs.add(
                    Job.createDropOffJob(it.id,
                        it.pos,
                        RESOURCE_ENERGY,
                        it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                        JobType.DROP_OFF_ENERGY
                    )
                )
            }
        }

        val constructionSite = room.find(FIND_CONSTRUCTION_SITES)
        constructionSite.sortByDescending { it.progress }
        constructionSite.forEach {
            jobs.add(
                Job.createDropOffJob(it.id,
                    it.pos,
                    RESOURCE_ENERGY,
                    it.progressTotal,
                    JobType.BUILD
                )
            )
        }

        // upgrade Controller
        room.controller?.let {
            Job.createDropOffJob(
                it.id,
                it.pos,
                RESOURCE_ENERGY,
                -1,
                JobType.UPGRADE_CONTROLLER
            )
        }?.let {
            jobs.add(
                it
            )
        }

        // Log
        jobs.forEach { j ->
            log(LogLevel.DEBUG,"Job found ${j.job_id}","processIdleCreeps","")
            var jobs: Array<Job> = room.memory.jobs
            jobs.forEach {
                if (it.job_id == j.job_id) {
                    log(LogLevel.DEBUG,"Existing Job found ${j.job_id}","processIdleCreeps","")
                    j.assignedCreeps = it.assignedCreeps
                }
            }
        }


        //
        ecoCreepsIdle.forEach { idleCreep ->
            var j: Job? = jobs.find { JobType.valueOf(it.jobType).validateCreep(it, idleCreep) }

            if (j != null) {
                log(LogLevel.INFO,"Job found ${j.job_id} for ${idleCreep.name}","processIdleCreeps","${idleCreep.name}")
                idleCreep.memory.job_id = j.job_id
                jobs.find { it.job_id == j.job_id }!!.assignedCreeps += idleCreep.name
                idleCreep.memory.task = JobType.valueOf(j.jobType).startingTask.name
            }
        }

        // Save
        room.memory.jobs = jobs.toTypedArray()
    }

    fun processCreeps() {
        ecoCreeps.forEach {
            it.performTask()
        }
    }

    private fun processRoom(room: Room) {
        // Look for new tasks
        // Assign task if idle
        log(
            LogLevel.INFO,
            "Room - ${room.name}; energyAvailable: ${room.energyAvailable}; creeps: ${room.find(FIND_MY_CREEPS).size} ",
            "EcoManager",
            room.name
        )


        if (room.getJobRequireingCreeps() > 0 && room.find(FIND_MY_CREEPS).size < 6) {
            val mainSpawn = room.find(FIND_MY_SPAWNS)[0]

            if (room.find(FIND_MY_CREEPS).isNotEmpty()
                && room.energyAvailable < 500000 // TODO: Max worker Size
                && room.energyAvailable < room.energyCapacityAvailable
            ){ return } // Wait for spawn to be full before building

            val bodyMakeUp = listOf<BodyPartConstant>(WORK, MOVE, CARRY)
            val body = buildBody(bodyMakeUp,bodyMakeUp, room.energyAvailable)
            log(LogLevel.INFO, body.toString(), "EcoManager", "run")

            spawnCreep(body, mainSpawn, CreepRole.WORKER)
        }
    }

    private fun spawnCreep(body: List<BodyPartConstant>,
                           spawn: StructureSpawn,
                           role: CreepRole
    ) {
        spawn.spawnCreep(body.toTypedArray(), "${role.name}_${Game.time}", options {
            memory = jsObject<CreepMemory> {
                this.role = role.name
                this.task = CreepTask.IDLE.name
                this.homeRoom = spawn.room.name
                this.type = CreepType.ECO.name
            }
        }
        )
    }
}


