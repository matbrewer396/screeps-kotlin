package room

import job.JobType
import memory.jobs
import screeps.api.*

fun Room.getJobRequireingCreeps(): Int {
    return memory.jobs.filter {
        JobType.valueOf(it.jobType).spawnCreepsIfAssignedLessThen >= it.assignedCreeps.size
    }.size
}