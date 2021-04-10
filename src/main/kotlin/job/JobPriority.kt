package job

import creep.CreepRole
import creep.role
import memory.role
import room.getCreepOfRole
import screeps.api.Creep
import screeps.api.RESOURCE_ENERGY
import screeps.api.Room
import screeps.api.*

var prioityStructureConstant = listOf<StructureConstant>(STRUCTURE_SPAWN, STRUCTURE_EXTENSION)

fun jobPriority(job: Job, creep: Creep, room: Room):Int {
    var default: Int = 600000
    return when (JobType.valueOf(job.jobType)){
        JobType.HARVEST_SOURCE      -> default

        JobType.DROP_OFF_ENERGY     ->

            if (creep.role() == CreepRole.CARRIER && prioityStructureConstant.contains(job.structureType) ) {
                1000
            }
            // Room starting off or all CARRIER died
            else if (
                creep.role() == CreepRole.WORKER
                && prioityStructureConstant.contains(job.structureType)
                && ( room.getCreepOfRole(CreepRole.CARRIER) == 0 || room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) ?: 9999999 < 5000
                )
            ) {
                1000
            }
            else {default}

        JobType.UPGRADE_CONTROLLER  ->
            if (room.controller?.level ?: 0 == 1 || room.controller?.ticksToDowngrade ?: 999999 < 4000) {
                1200
            } else {
                default
            }

        JobType.BUILD               -> when (job.structureType) {
            STRUCTURE_SPAWN -> 4011
            STRUCTURE_EXTENSION -> 4012
            STRUCTURE_TOWER -> 4013
            STRUCTURE_CONTAINER -> 3014

            STRUCTURE_STORAGE -> 4500
            STRUCTURE_ROAD -> if (job.subJobType == SubJobType.SWAMP && room.controller?.level ?: 0 == 1) {
                1100
            } else if (job.subJobType == SubJobType.SWAMP) {
                4005
            } else {
                4950
            }

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
