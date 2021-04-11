package creep

import job.Job
import job.JobType
import memory.jobs
import room.getCreepOfRole
import screeps.api.*

enum class CreepRole(val noRequired: (room: Room) -> Int,
                     val canRefileSpawn: Boolean,
                     val maxBodySize: Int,
                     val startingBody: List<BodyPartConstant>,
                     val recurringBody: List<BodyPartConstant>,
                     val spawnFromRCL: Int
                     ) {
    WORKER(::calculateMaxWorker, true, 3350,
        listOf<BodyPartConstant>(WORK, MOVE, CARRY),listOf<BodyPartConstant>(WORK, MOVE, CARRY),1),
    MINER(::calculateMaxMiner,true, 750,
        listOf<BodyPartConstant>(WORK, MOVE),listOf<BodyPartConstant>(WORK),2),
    CARRIER(::calculateMaxCarrier, false, 2600
        ,listOf<BodyPartConstant>(MOVE, CARRY),listOf<BodyPartConstant>(MOVE, CARRY),2)
}

fun calculateMaxWorker(room: Room):Int {
    var j = room.memory.jobs
        .filter { listOf<JobType>(JobType.BUILD, JobType.REPAIR).contains(JobType.valueOf(it.jobType))
                && it.assignedCreeps.isEmpty()}
        .size / 3

    var storage: Int = room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) ?: 0
    if (j == 0) {
        if (storage > 150000 && room.controller?.level ?: 10 <= 8) {
            storage -= 150000
            return if (storage < 200000) {
                2
            } else if (storage < 250000){
                3
            } else if (storage < 350000){
                5
            } else (8)
        }
        return 1
    } else if (room.controller?.level ?: 0 >= 5) {
        if (storage > 50000) {
            if (storage > 150000) {
                if (j < 8 && room.controller?.level ?: 10 <= 8) {return (8)}
            }
            return j
        }
    }
    return 1

}

fun calculateMaxMiner(room: Room):Int {
    if (room.getCreepOfRole(CreepRole.CARRIER)==0) {return 1}
    return room.find(FIND_SOURCES).size
}

fun calculateMaxCarrier(room: Room):Int {
    if (room.controller?.level ?: 0 >= 5) {
        var r = room.memory.jobs
            .filter { listOf<JobType>(JobType.DROP_OFF_ENERGY, JobType.PICKUP, JobType.WITHDRAW).contains(JobType.valueOf(it.jobType))
                    && it.structureType !== STRUCTURE_STORAGE}
            .sumBy { it.requestedUnit ?: 0 }

        if (r > 25000) {
            return 3
        } else if (r > 1000) {
            return 2
        } else if (r > 1000) {
            return 1
        }
    } else {
        //TODO
        return  1
    }

    return 0
}