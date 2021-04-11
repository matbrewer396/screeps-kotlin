package creep

import job.Job
import job.JobType
import job.SubJobType
import log.LogLevel
import log.log
import memory.*
import screeps.api.*


fun Creep.getJob(): Job? {
    return if (memory.job_id == "Renew-${name}") {
        Job.createRenewJob(this)
    } else {
        room.memory.jobs.find{ it.job_id == memory.job_id}
    }


}

fun Creep.completeJob(job: Job) {
    log(LogLevel.DEBUG,"Complete Job ${memory.job_id} - ${job.jobType} - ${job.subJobType}","completeJob",name)

    if ( memory.job_id !== "") {  removeFromJob(memory.job_id) }
}
fun Creep.removeFromJob(jobId: String) {
    for ((_, room) in Game.rooms) {
        room.memory.jobs.forEach { job ->
            if (job.job_id == jobId) {
                job.assignedCreeps = job.assignedCreeps.filter { it.creepName !== name }.toTypedArray()
            }
            //To reveal job, does still need top exists?
        }
    }
    memory.lastJob_id = memory.job_id
    memory.job_id = ""
}

fun Creep.role(): CreepRole {
    return CreepRole.valueOf(memory.role)
}


fun Creep.getBodyCost():Int{
    return this.body.sumBy { BODYPART_COST[it.type] ?: 0 }

}

fun Creep.canIBeBigger():Boolean{
    val size = getBodyCost()
    if (size >= role().maxBodySize  ) { return false }
    if (size < room.energyAvailable) { return true }
    return false
}