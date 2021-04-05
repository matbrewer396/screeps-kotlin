package creep

import job.Job
import log.LogLevel
import log.log
import memory.*
import screeps.api.*

fun Creep.getTask(): CreepTask{ return CreepTask.valueOf(memory.task) }
fun Creep.setTask(creepTask:CreepTask) {
    memory.task = creepTask.name
    say(creepTask.name)
}

fun Creep.getJob(): Job? {
    return room.memory.jobs.find{ it.job_id == memory.job_id}
}

fun Creep.completeJob() {
    log(LogLevel.DEBUG,"Complete Job ${memory.job_id}","completeJob",name)
    if ( memory.job_id !== "") {  removeFromJob(memory.job_id) }
}
fun Creep.removeFromJob(jobid: String) {
    for ((_, room) in Game.rooms) {
        room.memory.jobs.forEach { job ->
            if (job.job_id == jobid) {
                job.assignedCreeps = job.assignedCreeps.filter { it !== name }.toTypedArray()
            }
            //To reval job, does still need top exists?
        }
    }

    setTask(CreepTask.IDLE)
    memory.job_id = ""
}