package job

import creep.CreepTask
import screeps.api.Creep
import screeps.api.RESOURCE_ENERGY
import screeps.api.ResourceConstant
import screeps.api.RoomPosition
// All job types
enum class JobType(val startingTask: CreepTask,
                   val validateCreep: (job: Job, creep: Creep) -> Boolean,
                   val spawnCreepsIfAssignedLessThen: Int
) {
    HARVEST_SOURCE(CreepTask.HARVEST, ::validateHarvest, -1),
    DROP_OFF_ENERGY(CreepTask.DROPOFF, ::validateDropOff, 1),
    UPGRADE_CONTROLLER(CreepTask.DROPOFF, ::validateDropOff, 1),
    BUILD(CreepTask.DROPOFF, ::validateBuild, 1),
}
//Check creep is valid for this type
fun validateHarvest(job: Job, creep: Creep) :Boolean{
    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 2) { return false }
    return true
}
fun validateDropOff(job: Job, creep: Creep) :Boolean{
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 2) { return false }
    return true
}
fun validateBuild(job: Job, creep: Creep) :Boolean{
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0){ return false }
    if (job.assignedCreeps.size > 1) { return false }
    return true
}




data class Job (
    var target_id: String,
    var roomPos: RoomPosition,
    var jobType: String,
    var assignedCreeps: Array<String>,
    var job_id: String,
    var resource: ResourceConstant?,
    var requestedUnit: Int?
) {
    companion object {
        fun create(target_id: String,roomPos: RoomPosition,jobType: JobType) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, null, null)
        }
        fun createDropOffJob(target_id: String,roomPos: RoomPosition, resource: ResourceConstant, requestedUnit: Int,jobType: JobType) :Job {
            var jobId: String = "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
            return Job(target_id,roomPos,jobType.name,arrayOf(),jobId, resource, requestedUnit)
        }
    }

    fun getJobType (): JobType {
        return JobType.valueOf(jobType)
    }

//    fun getJobId():String {
//        return "${roomPos.roomName}-${jobType.name}-${roomPos.x}-${roomPos.y}"
//    }




}
