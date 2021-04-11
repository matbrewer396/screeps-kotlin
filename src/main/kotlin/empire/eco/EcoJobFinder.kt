package empire.eco

import creep.CreepRole
import job.*
import memory.role
import screeps.api.*
import screeps.api.structures.*

class EcoJobFinder {

    fun findMiningStations(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_STRUCTURES)
            .filter { it.structureType == STRUCTURE_CONTAINER }
            // TODO: and next to source
            .forEach {
                jobs.add(
                    Job.createSimple(
                        room,
                        it.id,
                        it.pos,
                        JobType.MINING_STATION
                    )
                )
            }
        return jobs
    }

    fun containerToWithDraw(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_STRUCTURES)
            .filter { it.structureType == STRUCTURE_CONTAINER }
            .unsafeCast<List<StructureContainer>>()
            .forEach {
                if (it.store.getUsedCapacity(RESOURCE_ENERGY)> 10) {
                    jobs.add(
                        Job.createJob(
                            room = room,
                            target_id = it.id,
                            roomPos = it.pos,
                            resource = RESOURCE_ENERGY,
                            requestedUnit = it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0,
                            jobType = JobType.WITHDRAW,
                            subJobType = SubJobType.STRUCTURE_CONTAINER,
                            structureType = it.structureType
                        )
                    )
                }
            }

        room.find(FIND_STRUCTURES)
            .filter { it.structureType == STRUCTURE_STORAGE }
            .unsafeCast<List<StructureStorage>>()
            .forEach {
                if (it.store.getUsedCapacity(RESOURCE_ENERGY)> 500) {
                    jobs.add(
                        Job.createJob(
                            room = room,
                            target_id = it.id,
                            roomPos = it.pos,
                            resource = RESOURCE_ENERGY,
                            requestedUnit = it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0,
                            jobType = JobType.WITHDRAW,
                            subJobType = SubJobType.STRUCTURE_STORAGE,
                            structureType = it.structureType
                        )
                    )
                }
            }
        return jobs
    }


    fun findSourcesToHarvest(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_SOURCES).forEach { source ->
            // Minder near by
            if (source.pos.findInRange(FIND_MY_CREEPS,2)
                    .filter { it.memory.role == CreepRole.MINER.name }
                    .isNotEmpty()
            ) {return@forEach}

            if (source.energy > 0) {
                jobs.add(
                    Job.createSimple(
                        room = room,
                        source.id,
                        source.pos,
                        JobType.HARVEST_SOURCE
                    )
                )
            }
        }
        return jobs
    }

    fun refillSpawn(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_MY_SPAWNS).forEach {
            if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                jobs.add(
                    Job.createJob(
                        room = room,
                        it.id,
                        it.pos,
                        RESOURCE_ENERGY,
                        it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                        JobType.DROP_OFF_ENERGY,
                        subJobType = SubJobType.STRUCTURE_SPAWN,
                        structureType = it.structureType
                    )
                )
            }
        }

        room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_EXTENSION }
            .unsafeCast<List<StructureExtension>>().forEach {
                if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                    jobs.add(
                        Job.createJob(
                            room = room,
                            it.id,
                            it.pos,
                            RESOURCE_ENERGY,
                            it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                            JobType.DROP_OFF_ENERGY,
                            subJobType = SubJobType.STRUCTURE_EXTENSION,
                            structureType = it.structureType
                        )
                    )
                }
            }
        return jobs
    }

    fun refillTowers(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_TOWER }
            .unsafeCast<List<StructureTower>>().forEach {
                if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 100) {
                    jobs.add(
                        Job.createJob(
                            room = room,
                            it.id,
                            it.pos,
                            RESOURCE_ENERGY,
                            it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                            JobType.DROP_OFF_ENERGY,
                            subJobType = SubJobType.NONE,
                            structureType = it.structureType
                        )
                    )
                }
            }
        return jobs
    }

    fun storage(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_STORAGE }
            .unsafeCast<List<StructureTower>>().forEach {
                if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                    jobs.add(
                        Job.createJob(
                            room = room,
                            it.id,
                            it.pos,
                            RESOURCE_ENERGY,
                            it.store.getFreeCapacity(RESOURCE_ENERGY) as Int,
                            JobType.DROP_OFF_ENERGY,
                            subJobType = SubJobType.STRUCTURE_STORAGE,
                            structureType = it.structureType
                        )
                    )
                }
            }
        return jobs
    }

    fun constructionSite(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        val constructionSite = room.find(FIND_CONSTRUCTION_SITES)
        constructionSite.sortByDescending { it.progress }
        constructionSite.forEach {

            var subJobType:SubJobType = SubJobType.NONE
            if (it.structureType == STRUCTURE_ROAD) {
                if (it.pos.lookFor(LOOK_TERRAIN) == TERRAIN_SWAMP) {
                    subJobType = SubJobType.SWAMP
                }
            }

            jobs.add(
                Job.createJob(
                    room = room,
                    it.id,
                    it.pos,
                    RESOURCE_ENERGY,
                    it.progressTotal,
                    JobType.BUILD,
                    subJobType,
                    it.structureType
                )
            )
        }
        return jobs
    }

    fun repairStrictures(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.find(FIND_STRUCTURES)
            .filter {
                (!listOf<StructureConstant>(STRUCTURE_WALL, STRUCTURE_CONTROLLER, STRUCTURE_RAMPART).contains(it.structureType)
                    && it.hits < (it.hitsMax * 0.90)
                        )
                || (it.structureType == STRUCTURE_RAMPART
                        && it.hits < (it.hitsMax * when (room.controller?.level ?:0) {
                            5 -> 0.005
                            6 -> 0.005
                            7 -> 0.005
                            8 -> 0.010
                            else -> 0.00
                        }
                        )
                    )


            }
            .sortedBy { when (it.structureType) {
                STRUCTURE_RAMPART -> 100000000
                STRUCTURE_WALL -> 1000000000
                else -> it.hits / it.hitsMax
            } }
            .forEach {
                if (jobs.size > 20) {return@forEach} // cause high cpu
                jobs.add(
                    Job.createJob(
                        room = room,
                        it.id,
                        it.pos,
                        RESOURCE_ENERGY,
                        (it.hitsMax - it.hits),
                        JobType.REPAIR,
                        SubJobType.NONE,
                        structureType = it.structureType
                    )
                )
            }
        return jobs
    }

    fun upgradeController(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()
        room.controller?.let {
            Job.createJob(
                room = room,
                it.id,
                it.pos,
                RESOURCE_ENERGY,
                -1,
                JobType.UPGRADE_CONTROLLER,
                SubJobType.NONE,
                structureType = it.structureType
            )
        }?.let {
            jobs.add(
                it
            )
        }
        return jobs
    }

    fun abandonedResources(room: Room): MutableList<Job> {
        var jobs = mutableListOf<Job>()

        room.find(FIND_DROPPED_RESOURCES)
            .forEach {
                if ( (it.resourceType === RESOURCE_ENERGY && it.amount> 50)
                    || it.resourceType !== RESOURCE_ENERGY
                ) { // TODO: Time remaining logic
                    jobs.add(
                        Job.createJob(
                            room = room,
                            target_id = it.id,
                            roomPos = it.pos,
                            resource = it.resourceType,
                            requestedUnit = it.amount,
                            jobType = JobType.PICKUP,
                            subJobType = SubJobType.NONE,
                            structureType = null
                        )
                    )
                }
            }
        room.find(FIND_TOMBSTONES)
            .forEach {
                if (it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0 > 50) { // TODO: Time remaining logic
                    jobs.add(
                        Job.createJob(
                            room = room,
                            target_id = it.id,
                            roomPos = it.pos,
                            resource = RESOURCE_ENERGY,
                            requestedUnit = it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0,
                            jobType = JobType.WITHDRAW,
                            subJobType = SubJobType.TOMBSTONES,
                            structureType = null
                        )
                    )
                }
            }
//        room.find(FIND_TOMBSTONES)
//            .forEach {
//                val s = it.store.get()
//                if (it.store.getUsedCapacity() ?: 0 > 50) { // TODO: Time remaining logic
//                    jobs.add(
//                        Job.createJob(
//                            room = room,
//                            target_id = it.id,
//                            roomPos = it.pos,
//                            resource = RESOURCE_ENERGY,
//                            requestedUnit = it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0,
//                            jobType = JobType.WITHDRAW,
//                            subJobType = SubJobType.TOMBSTONES,
//                            structureType = null
//                        )
//                    )
//                }
//            }

        room.find(FIND_RUINS)
            .forEach {
                if (it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0 > 0) { // TODO: Time remaining logic
                    jobs.add(
                        Job.createJob(
                            room = room,
                            target_id = it.id,
                            roomPos = it.pos,
                            resource = RESOURCE_ENERGY,
                            requestedUnit = it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0,
                            jobType = JobType.WITHDRAW,
                            subJobType = SubJobType.RUINS,
                            structureType = null
                        )
                    )
                }
            }

        jobs.sortByDescending { it.requestedUnit }
        return jobs
    }


}