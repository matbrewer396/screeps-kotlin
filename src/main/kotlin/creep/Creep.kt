package creep

import job.Job
import job.JobType
import job.SubJobType
import log.LogLevel
import log.log
import memory.*
import screeps.api.*


fun Creep.getJob(): Job? {
    return room.memory.jobs.find{ it.job_id == memory.job_id}
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
    memory.job_id = ""
}

fun Creep.role(): CreepRole {
    return CreepRole.valueOf(memory.role)
}