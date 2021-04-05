package empire

import log.*
import memory.job_id
import memory.jobs
import screeps.api.*
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete

class EmpireManager {
    /**
     * Main fun to start everything
     */
    fun runEmpire() {
        log(LogLevel.ALWAYS, "Starting ---------------------------------------------------------------------------------------", "EmpireManager", "runEmpire")

        var em = EcoManager()
        em.run()

        //delete memories of creeps that have passed away
        cleanUpTheDead(Game.creeps)
    }


}

private fun cleanUpTheDead(creeps: Record<String, Creep>) {
    if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {  removeDeadCreep(creepName)   }
        else if (creeps[creepName]?.hits ?: 1 == 0 && creeps[creepName]?.spawning != true){  removeDeadCreep(creepName)   }
    }

}

private fun removeDeadCreep(creepName: String) {
    log(LogLevel.INFO, "deleting obsolete memory entry for creep $creepName", "EmpireManager", "removeDead")
    //Remove from job
    val jobId = Memory.creeps[creepName]?.job_id
    if (jobId !== null) {
        for ((_, room) in Game.rooms) {
            room.memory.jobs.forEach {
                if (it.assignedCreeps.contains(creepName)) {
                    it.assignedCreeps = it.assignedCreeps.filter { it !== creepName }.toTypedArray()
                }
            }
        }
    }
    delete(Memory.creeps[creepName])
}