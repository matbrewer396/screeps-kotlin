package empire.eco

import job.*
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
        room.find(FIND_SOURCES).forEach {
            if (it.energy > 0) {
                jobs.add(
                    Job.createSimple(
                        it.id,
                        it.pos,
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
                if (it.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
                    jobs.add(
                        Job.createJob(
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
            it.structureType
            jobs.add(
                Job.createJob(
                    it.id,
                    it.pos,
                    RESOURCE_ENERGY,
                    it.progressTotal,
                    JobType.BUILD,
                    SubJobType.NONE,
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
                it.structureType !== STRUCTURE_WALL
                        && it.structureType !== STRUCTURE_CONTROLLER
                        && it.hits < (it.hitsMax * 0.90)
            }
            .forEach {
                jobs.add(
                    Job.createJob(
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
                if (it.amount> 50) { // TODO: Time remaining logic
                    jobs.add(
                        Job.createJob(
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

        room.find(FIND_RUINS)
            .forEach {
                if (it.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0 > 0) { // TODO: Time remaining logic
                    jobs.add(
                        Job.createJob(
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