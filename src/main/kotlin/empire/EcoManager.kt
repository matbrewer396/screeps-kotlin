package empire

import creep.*
import creep.CreepType
import creep.eco.EcoCreep
import creep.eco.MinerCreep
import creep.eco.WorkerCreep
import empire.eco.EcoJobFinder
import empire.eco.buildBody
import job.AssignmentEntry
import job.Job
import job.JobType
import job.ResourceTransferDirection
import log.LogLevel
import log.log
import memory.*
import room.getJobRequireingCreeps
import room.isUnderAttck
import screeps.api.*
import screeps.api.structures.*
import screeps.utils.toMap
import screeps.utils.unsafe.jsObject
import kotlin.math.abs

class EcoManager {
    private val ecoCreeps = mutableListOf<EcoCreep>()
    private val ecoCreepsIdle = mutableListOf<Creep>()
    private val towers = mutableListOf<StructureTower>()

    fun run() {
        //delete(Memory.rooms[room.name])
//        for ((_, room) in Game.rooms) {
//            room.memory.jobs = arrayOf<Job>()
//        }
        initializeWorkerCreeps()
        initializeTower()

        processIdleCreeps()
        processCreeps()


        //Process Rooms
        for ((_, room) in Game.rooms) {
            processRoom(room)

            if (Game.time >= room.memory.planNextCheck) {
                val bm = BaseBuilder()
                bm.planMaker(room)
            }


        }
    }
    fun initializeTower() {
        Game.rooms.toMap().forEach { m ->
            if (m.value.isUnderAttck()) {return}
            m.value.find(FIND_MY_STRUCTURES)
                .filter { it.structureType == STRUCTURE_TOWER }
                .unsafeCast<List<StructureTower>>()
                .forEach {
                    if (it.store.getUsedCapacity(RESOURCE_ENERGY) == 0 ) { return@forEach } // tower empty dont process
                    towers.add(it)
                }
        }

    }


    /*
    * Assign Creep to queues
    * */
    fun initializeWorkerCreeps() {
        for (creep in Game.creeps.values) {
            if (creep.my == false) {
                continue
            }

            if (creep.spawning) {
                continue
            }

            if (creep.memory.type == "") {
                creep.suicide()
            }
            if (creep.memory.type !== CreepType.ECO.name) {
                continue
            }

            when (creep.memory.role) {
                CreepRole.WORKER.name -> ecoCreeps.add(WorkerCreep(creep))
                CreepRole.CARRIER.name -> ecoCreeps.add(WorkerCreep(creep))
                CreepRole.MINER.name -> ecoCreeps.add(MinerCreep(creep))
            }
            if (creep.memory.job_id.isBlank()) {
                ecoCreepsIdle.add(creep)
            }

        }
    }

    fun processIdleCreeps() {
        if (ecoCreepsIdle.isEmpty()) {
            return
        }
        log(LogLevel.DEBUG, "Processing ${ecoCreepsIdle.size} idle creep", "processIdleCreeps", "")

        var jobs = mutableListOf<Job>()
        //Look for jobs
        var room: Room = ecoCreepsIdle[0].room
        val jobFinder = EcoJobFinder()


        jobs = (jobs
                + jobFinder.findMiningStations(room)
                // Get Energy
                + jobFinder.abandonedResources(room)
                + jobFinder.containerToWithDraw(room)
                + jobFinder.findSourcesToHarvest(room)
                // Refill
                + jobFinder.refillSpawn(room)
                + jobFinder.refillTowers(room)
                //Work
                + jobFinder.constructionSite(room)
                + jobFinder.repairStrictures(room)
                + jobFinder.upgradeController(room)
                + jobFinder.storage(room)

            ) as MutableList<Job>



        // Find existing job
        jobs.forEach { j ->
            log(LogLevel.DEBUG, "Job found ${j.job_id}", "processIdleCreeps", "")
            var memJobs: Array<Job> = room.memory.jobs
            memJobs.forEach { memJob ->
                if (memJob.job_id == j.job_id) {
                    log(LogLevel.DEBUG, "Existing Job found ${j.job_id}", "processIdleCreeps", "")
                    memJob.assignedCreeps.forEach {
                        if (Game.creeps[it.creepName] !== undefined) { // Check creep is still alive
                            j.assignedCreeps += AssignmentEntry(it.creepName, it.reservedUnit)
                        }
                    }
                }
            }
        }


        // Assign creep to Job
        log(LogLevel.DEBUG, " Assigning creeps to Job", "processIdleCreeps", "")
        ecoCreepsIdle.forEach { idleCreep ->
            var j: Job? = jobs.filter { JobType.valueOf(it.jobType).validateCreep(it, idleCreep) }
                .minByOrNull { JobType.valueOf(it.jobType).jobPriority(it,idleCreep,room) +
                    abs(it.roomPos.x - idleCreep.pos.x) + abs(it.roomPos.y - idleCreep.pos.y) }

            if (j != null) {
                log(
                    LogLevel.INFO,
                    "Job found ${j.job_id} for ${idleCreep.name}",
                    "processIdleCreeps",
                    "${idleCreep.name}"
                )
                idleCreep.memory.job_id = j.job_id
                jobs.find { it.job_id == j.job_id }!!.assignedCreeps += AssignmentEntry(idleCreep.name,
                    when (JobType.valueOf(j.jobType).resourceTransferDirection) {
                        ResourceTransferDirection.OUTBOUND -> idleCreep.store.getUsedCapacity(j.resource ?: RESOURCE_ENERGY)
                        ResourceTransferDirection.INBOUND -> idleCreep.store.getFreeCapacity(j.resource ?: RESOURCE_ENERGY)
                    }
                )
            }
        }

        log(
            LogLevel.INFO,
            "Job queue rebuild: ${jobs.size}, unassigned ${jobs.filter { it.assignedCreeps.isEmpty() }.size}",
            "processIdleCreeps",
            ""
        )
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

        if (room.find(FIND_MY_CREEPS).isNotEmpty()
            && room.energyAvailable < 500000 // TODO: Max worker Size
            && room.energyAvailable < room.energyCapacityAvailable
        ) {
            return
        } //Wait for spawn to be full before building
        val mainSpawn = room.find(FIND_MY_SPAWNS)[0]

        if (room.getJobRequireingCreeps() > 0
            && room.find(FIND_MY_CREEPS).filter { it.memory.role == CreepRole.WORKER.name }.size < CreepRole.WORKER.maxNumber) {
            val energyAvailable = if (room.energyAvailable > CreepRole.CARRIER.maxBodySize) {CreepRole.CARRIER.maxBodySize} else {room.energyAvailable}
            val bodyMakeUp = listOf<BodyPartConstant>(WORK, MOVE, CARRY)
            val body = buildBody(bodyMakeUp, bodyMakeUp, energyAvailable)


            spawnCreep(body, mainSpawn, CreepRole.WORKER, null)
        }

        // Create minder
        if (room.memory.jobs
                .filter {it.jobType == JobType.MINING_STATION.name
                    && it.assignedCreeps.isEmpty()
            }.isNotEmpty()
            && room.find(FIND_SOURCES).size > room.find(FIND_MY_CREEPS).filter { it.memory.role == CreepRole.MINER.name }.size
        ){
            val energyAvailable = if (room.energyAvailable > CreepRole.CARRIER.maxBodySize) {CreepRole.CARRIER.maxBodySize} else {room.energyAvailable}
            val body = buildBody(listOf<BodyPartConstant>(WORK, MOVE), listOf<BodyPartConstant>(WORK), energyAvailable)
            spawnCreep(body, mainSpawn, CreepRole.MINER,
                room.memory.jobs.first { it.jobType == JobType.MINING_STATION.name && it.assignedCreeps.isEmpty() }
                    .job_id
            )
        }

        //Create Carriers
        if (room.find(FIND_MY_CREEPS).filter { it.memory.role == CreepRole.CARRIER.name }.size < CreepRole.CARRIER.maxNumber
            && room.controller?.level ?: 0 >= 3){
            val energyAvailable = if (room.energyAvailable > CreepRole.CARRIER.maxBodySize) {CreepRole.CARRIER.maxBodySize} else {room.energyAvailable}
            val body = buildBody(listOf<BodyPartConstant>(CARRY, MOVE), listOf<BodyPartConstant>(CARRY, MOVE), energyAvailable)
            spawnCreep(body, mainSpawn, CreepRole.CARRIER, null )
        }
    }

    private fun spawnCreep(
        body: List<BodyPartConstant>,
        spawn: StructureSpawn,
        role: CreepRole,
        jobId: String?
    ) {
        log(LogLevel.INFO, "${role.name}_${Game.time} - ${body.toString()}", "EcoManager", "spawnCreep")
        spawn.spawnCreep(body.toTypedArray(), "${role.name}_${Game.time}", options {
            memory = jsObject<CreepMemory> {
                this.role = role.name
                this.homeRoom = spawn.room.name
                this.type = CreepType.ECO.name
                this.job_id = jobId ?: ""
            }
        })
        // Assign job
        if (jobId !== null) {
            spawn.room.memory.jobs
                .find { it.job_id == jobId }!!
                .assignedCreeps = arrayOf<AssignmentEntry>(AssignmentEntry("${role.name}_${Game.time}", null))
        }
    }
}



