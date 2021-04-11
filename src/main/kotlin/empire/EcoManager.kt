package empire

import config.*
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
import room.getCreepOfRole
import room.isUnderAttck
import screeps.api.*
import screeps.api.structures.*
import screeps.utils.toMap
import screeps.utils.unsafe.jsObject
import kotlin.math.abs
import kotlin.enumValues as kotlinEnumValues

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
        processTowers()


        //Process Rooms
        for ((_, room) in Game.rooms) {
            processRoom(room)

            if (Game.time >= room.memory.planNextCheck || forcePlanOnEveryTick) {
                val bm = BaseBuilder()
                bm.planMaker(room)
            }


        }
    }
    private fun initializeTower() {
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

    private fun processTowers() {
        towers.forEach { tower ->
            if (tower.room.isUnderAttck()) { return@forEach }
            if (tower.store.getUsedCapacity(RESOURCE_ENERGY) < 300 ) { return@forEach } // Save for an attack

            var repairJob: Job = tower.room.memory.jobs
                        .filter { job ->
                            job.jobType == JobType.REPAIR.name
                                    && Game.getObjectById<Structure>(job.target_id)?.hits ?: 0 < (Game.getObjectById<Structure>(
                                job.target_id
                            )?.hitsMax ?: 0) * 0.95
                        }.minByOrNull { job -> abs(tower.pos.x - job.roomPos.x) + abs(tower.pos.y - job.roomPos.y) } ?: return@forEach
            //TODO: sort by range
            Game.getObjectById<Structure>(repairJob.target_id)?.let { tower.repair(it) }

        }
    }


    /*
    * Assign Creep to queues
    * */
    private fun initializeWorkerCreeps() {
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

    private fun processIdleCreeps() {
        if (ecoCreepsIdle.isEmpty()) { return   }
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

        log(LogLevel.DEBUG, "Jobs found (${jobs.size})", "processIdleCreeps", "")


        kotlinEnumValues<JobType>().forEach { jT ->
            log(LogLevel.INFO, "Jobs found of type ${jT.name} - (${jobs.filter { it.jobType == jT.name }.size})", "processIdleCreeps", "")
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
                // Assign creeps to job
                idleCreep.memory.job_id = j.job_id
                jobs.find { it.job_id == j.job_id }!!.assignedCreeps += AssignmentEntry(idleCreep.name,
                    when (JobType.valueOf(j.jobType).resourceTransferDirection) {
                        ResourceTransferDirection.OUTBOUND -> idleCreep.store.getUsedCapacity(j.resource ?: RESOURCE_ENERGY)
                        ResourceTransferDirection.INBOUND -> idleCreep.store.getFreeCapacity(j.resource ?: RESOURCE_ENERGY)
                    }
                )
                // see if there is second job near by needingin a pickup
//                if (j.requestedUnit < idleCreep.store.getFreeCapacity()) {
//                    val secondaryJob = jobs.filter { it.job_id !== j.job_id
//                            && (abs(it.roomPos.x - j.roomPos.x) + abs(it.roomPos.y - j.roomPos.y)) < 4 // Near by
//                            && JobType.valueOf(it.jobType).resourceTransferDirection == ResourceTransferDirection.INBOUND
//                            && JobType.valueOf(it.jobType).validateCreep(it, idleCreep)
//                    }
//                    secondaryJob
//
//                }


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

    private fun processCreeps() {
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

        //Spawn Creep
        val mainSpawn = room.find(FIND_MY_SPAWNS)[0]
        // all creep dead?
        if (room.getCreepOfRole(CreepRole.WORKER) == 0
            && room.energyAvailable > 150
        ) {
            spawnCreepRole(CreepRole.WORKER,mainSpawn,room)
        }


        //For each creep spawn is can
        kotlinEnumValues<CreepRole>().forEach { creepRole ->
            //Only create creep if max size can be create
            if (room.energyAvailable < creepRole.maxBodySize && room.energyAvailable !== room.energyCapacityAvailable) return@forEach
            //spawn creep
            spawnCreepRole(creepRole,mainSpawn,room)
        }


        if (room.find(FIND_MY_CREEPS).isNotEmpty()
            && room.energyAvailable < 500000 // TODO: Max worker Size
            && room.energyAvailable < room.energyCapacityAvailable
        ) {
            return
        } //Wait for spawn to be full before building


    }

    private fun spawnCreepRole(creepRole: CreepRole, spawn: StructureSpawn, room: Room){
        //max count
        if (room.getCreepOfRole(creepRole) >= creepRole.noRequired(room)) {return}

        // Max size
        val energyAvailable = if (room.energyAvailable > creepRole.maxBodySize) {
            creepRole.maxBodySize
        } else {
            room.energyAvailable
        }
        val body = buildBody(creepRole.startingBody, creepRole.recurringBody, energyAvailable)

        //todo move this
        var jobId: String? = null
        if (creepRole == CreepRole.MINER) {
            jobId = room.memory.jobs
                .firstOrNull() { it.jobType == JobType.MINING_STATION.name && it.assignedCreeps.isEmpty() }
                ?.job_id
            if (jobId == null) return
        }

        spawnCreep(body, spawn, creepRole, jobId)
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



