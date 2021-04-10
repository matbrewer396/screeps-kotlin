package job

import screeps.api.Creep
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.*

fun jobPriority(job: Job, creep: Creep, room: Room):Int {

    return when (JobType.valueOf(job.jobType)){
        JobType.HARVEST_SOURCE      -> 600000
        JobType.DROP_OFF_ENERGY     -> 600000
        JobType.UPGRADE_CONTROLLER  -> 600000
        JobType.BUILD               -> when (job.structureType) {
            STRUCTURE_SPAWN -> 4001
            STRUCTURE_EXTENSION -> 4002
            STRUCTURE_TOWER -> 4003

            STRUCTURE_STORAGE -> 4500
            STRUCTURE_ROAD -> 4950
            STRUCTURE_WALL -> 4955
            STRUCTURE_RAMPART -> 4956
            else -> 4900
        }

        JobType.REPAIR              -> 3000
        JobType.MINING_STATION      -> 1000
        JobType.WITHDRAW            -> when (job.subJobType) {
            SubJobType.STRUCTURE_CONTAINER -> if (job.requestedUnit ?: 0 >= creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0 ){
                2010
            } else {
                2800
            }
            SubJobType.STRUCTURE_STORAGE -> 2600
            else -> 2500
        }
        JobType.PICKUP              -> 1000
    }

}
