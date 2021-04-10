package room

import creep.CreepRole
import job.JobType
import memory.homeRoom
import memory.jobs
import memory.role
import screeps.api.*
import screeps.utils.toMap

fun Room.getJobRequireingCreeps(): Int {
    return memory.jobs.filter {
        JobType.valueOf(it.jobType).spawnCreepsIfAssignedLessThen >= it.assignedCreeps.size
    }.size
}

enum class RoomHealth() {
    HEALTHY,
    LAST_CREEP_STAGING,
    OUT_OF_CREEPS
}

fun Room.caluateRoomHealth() :RoomHealth {
    var remainingCreeps = find(FIND_MY_CREEPS)
                        .filter { CreepRole.valueOf(it.memory.role).canRefileSpawn }
                        .size
    if ( remainingCreeps == 1) { return RoomHealth.LAST_CREEP_STAGING }
    if ( remainingCreeps == 0) { return RoomHealth.OUT_OF_CREEPS }

    return RoomHealth.HEALTHY
}

fun Room.isUnderAttck() :Boolean {
    return find(FIND_HOSTILE_CREEPS).isNotEmpty()
}

fun Room.getCreepOfRole(role: CreepRole): Int {
    return Game.creeps.toMap().filter {
        it.value.memory.role == role.name && it.value.memory.homeRoom == this.name
    }.size
}