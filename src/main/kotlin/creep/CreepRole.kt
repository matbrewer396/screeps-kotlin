package creep

import job.JobType
import memory.jobs
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
        ,listOf<BodyPartConstant>(MOVE, CARRY),listOf<BodyPartConstant>(WORK, CARRY),2)
}

fun calculateMaxWorker(room: Room):Int {
    var j = room.memory.jobs
        .filter { listOf<JobType>(JobType.BUILD, JobType.REPAIR).contains(JobType.valueOf(it.jobType))
                && it.assignedCreeps.isEmpty()}
        .size / 3

    if (j == 0) {
        if (room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) > 150000 && room.controller?.level ?: 10 <= 8) {
            return (8)
        }
        return 1
    } else if (room.controller?.level ?: 0 >= 5) {
        if (room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) > 50000) {
            if (room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) > 150000) {
                if (j < 8 && room.controller?.level ?: 10 <= 8) {return (8)}
            }
            return j
        }
    }
    return 2

}

fun calculateMaxMiner(room: Room):Int {
    return room.find(FIND_SOURCES).size
}

fun calculateMaxCarrier(room: Room):Int {
    return 2
}