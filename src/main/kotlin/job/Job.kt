package job

import creep.CreepRole
import memory.lastWithDrawStorageAt
import memory.role
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower

enum class ResourceTransferDirection() {
    INBOUND,
    OUTBOUND
}

enum class SubJobType() {
    NONE,
    STRUCTURE_CONTAINER,
    STRUCTURE_STORAGE,
    STRUCTURE_EXTENSION,
    STRUCTURE_SPAWN,
    TOMBSTONES,
    RUINS

}

// All job types
enum class JobType(val validateCreep: (job: Job, creep: Creep) -> Boolean,
                   val spawnCreepsIfAssignedLessThen: Int,
                   var resourceTransferDirection: ResourceTransferDirection,
                   val jobPriority: (job: Job, creep: Creep, room: Room) -> Int,
                   val validateTower: (job: Job, tower: StructureTower) -> Boolean
) {
    HARVEST_SOURCE(::validateHarvest,
        -1,
        ResourceTransferDirection.INBOUND,
        ::jobPriority,
        ::notTowerSuitable
        ),
    DROP_OFF_ENERGY(::validateDropOff,
        1,
        ResourceTransferDirection.OUTBOUND,
        ::jobPriority,
        ::notTowerSuitable),
    UPGRADE_CONTROLLER(::validateControllerUpgrade,
        1,
        ResourceTransferDirection.OUTBOUND,
        ::jobPriority,
        ::notTowerSuitable),
    BUILD(::validateBuild,
        1,
        ResourceTransferDirection.OUTBOUND,
        ::jobPriority,
        ::notTowerSuitable),
    REPAIR(::validateBuild,
        1,
        ResourceTransferDirection.OUTBOUND,
        ::jobPriority,
        ::validateTowerRepair),
    MINING_STATION(::validateMiningStation,
        1,
        ResourceTransferDirection.INBOUND,
        ::jobPriority,
        ::notTowerSuitable),
    WITHDRAW(::validateWithDraw,
        1,
        ResourceTransferDirection.INBOUND,
        ::jobPriority,
        ::notTowerSuitable),
    PICKUP(::validatePickUp,
        1,
        ResourceTransferDirection.INBOUND,
        ::jobPriority,
        ::notTowerSuitable)
}





data class Job (
    var target_id: String,
    var roomPos: RoomPosition,
    var jobType: String,
    var assignedCreeps: Array<AssignmentEntry>,
    var job_id: String,
    var resource: ResourceConstant?,
    var requestedUnit: Int?,
    var subJobType: SubJobType?,
    var structureType: StructureConstant?
) {
    companion object {
        fun createSimple(target_id: String, roomPos: RoomPosition, jobType: JobType) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, null, null,SubJobType.NONE, null)
        }
        fun createJob(target_id: String, roomPos: RoomPosition, resource: ResourceConstant, requestedUnit: Int, jobType: JobType, subJobType: SubJobType, structureType: StructureConstant?) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, resource, requestedUnit,subJobType,structureType)
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