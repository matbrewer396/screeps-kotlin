package job

import creep.CreepRole
import memory.lastWithDrawStorageAt
import memory.role
import screeps.api.*
import screeps.api.structures.Structure

//Check creep is valid for this type
fun validateHarvest(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) == 0){ return false }
    if ((creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) > 50){ return false }
    if (job.requestedUnit ?: 0 == 0) {
        if (job.assignedCreeps.size > 2) { return false }
    } else {
        if (job.assignedCreeps.sumOf { it.reservedUnit ?: 0} > job.requestedUnit ?: 0) {
            return false
        }
    }


    return true
}

fun validateDropOff(job: Job, creep: Creep) :Boolean{

    // Carrying more then energy
    if (creep.store.getUsedCapacity() !== creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0
        && job.structureType == STRUCTURE_STORAGE ) {
        return true
    }

    if (listOf<CreepRole>(CreepRole.WORKER, CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }
        && creep.room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) ?: 9999999 > 5000
    ) { return false }



    if (job.subJobType == SubJobType.STRUCTURE_STORAGE) {
        if (creep.memory.lastWithDrawStorageAt > Game.time - 20)  { return false }
        if (listOf<CreepRole>(CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
        // Drop anything
        if (creep.store.getUsedCapacity() == 0){ return false }
    } else {
        if (Game.rooms[job.roomPos.roomName]?.find(FIND_CREEPS)?.any { it.memory.role == CreepRole.CARRIER.name } == true
            && creep.memory.role !== CreepRole.CARRIER.name
        ){ return false }
        if (job.assignedCreeps.size > 1) { return false }
        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    }
    return true
}
fun validateControllerUpgrade(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    return true
}
fun validateBuild(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 3) { return false }
    return true
}
fun validateRepair(job: Job, creep: Creep) :Boolean{
    var o = Game.getObjectById<Structure>(job.target_id)
    if (o != null) {
        if (o.hits ?: 0 > (o.hitsMax ?: 0) * 0.7) {return false}
    }
    if (listOf<CreepRole>(CreepRole.WORKER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 1) { return false }
    return true
}
fun validateMiningStation(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.MINER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (job.assignedCreeps.size > 1) { return false }
    return true
}
fun validateWithDraw(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER, CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) == 0){ return false }
    if ((creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) > 50){ return false }
    if (job.requestedUnit > 0 && creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0 > 0 ) {
        var reservedUnits: Int = 0
        job.assignedCreeps.forEach { reservedUnits += it.reservedUnit ?: 0 }
        if ((reservedUnits + (creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0)) > job.requestedUnit
            && reservedUnits > 0
        ) {return false}
    }


    //if (job.assignedCreeps.size > 2) { return false }
    return true
}

fun validatePickUp(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER, CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) == 0){ return false }
    if ((creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) > 50){ return false }
    if (job.assignedCreeps.size > 2) { return false }
    return true
}
@Suppress("UNUSED_PARAMETER")
fun validateAllCreeps(job: Job, creep: Creep) :Boolean{
    return true
}