package job

import creep.CreepRole
import memory.role
import screeps.api.Creep
import screeps.api.RESOURCE_ENERGY
import screeps.api.ResourceConstant
import screeps.api.RoomPosition

enum class ResourceTransferDirection() {
    INBOUND,
    OUTBOUND
}

// All job types
enum class JobType(val validateCreep: (job: Job, creep: Creep) -> Boolean,
                   val spawnCreepsIfAssignedLessThen: Int,
                   var resourceTransferDirection: ResourceTransferDirection
) {
    HARVEST_SOURCE(::validateHarvest,
        -1,
        ResourceTransferDirection.INBOUND),
    DROP_OFF_ENERGY(::validateDropOff,
        1,
        ResourceTransferDirection.OUTBOUND),
    UPGRADE_CONTROLLER(::validateControllerUpgrade,
        1,
        ResourceTransferDirection.OUTBOUND),
    BUILD(::validateBuild,
        1,
        ResourceTransferDirection.OUTBOUND),
    REPAIR(::validateBuild,
        1,
        ResourceTransferDirection.OUTBOUND),
    MINING_STATION(::validateMiningStation,
        1,
        ResourceTransferDirection.INBOUND),
    WITHDRAW(::validateWithDraw,
        1,
        ResourceTransferDirection.INBOUND),
    PICKUP(::validatePickUp,
        1,
        ResourceTransferDirection.INBOUND),
}
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
    if (listOf<CreepRole>(CreepRole.WORKER, CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 1) { return false }
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
    if (job.assignedCreeps.size > 2) { return false }
    return true
}

fun validatePickUp(job: Job, creep: Creep) :Boolean{
    if (listOf<CreepRole>(CreepRole.WORKER, CreepRole.CARRIER).none { it == CreepRole.valueOf(creep.memory.role) }) { return false }
    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) == 0){ return false }
    if ((creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) > 50){ return false }
    if (job.assignedCreeps.size > 2) { return false }
    return true
}





data class Job (
    var target_id: String,
    var roomPos: RoomPosition,
    var jobType: String,
    var assignedCreeps: Array<AssignmentEntry>,
    var job_id: String,
    var resource: ResourceConstant?,
    var requestedUnit: Int?
) {
    companion object {
        fun createSimple(target_id: String, roomPos: RoomPosition, jobType: JobType) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, null, null)
        }
        fun createJob(target_id: String, roomPos: RoomPosition, resource: ResourceConstant, requestedUnit: Int, jobType: JobType) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, resource, requestedUnit)
        }
    }

    fun getJobType (): JobType {
        return JobType.valueOf(jobType)
    }

}

data class AssignmentEntry (
    var creepName: String,
    var reservedUnit: Int?
)