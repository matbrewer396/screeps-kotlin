package creep.eco


import creep.*
import job.Job
import job.JobType
import log.LogLevel
import log.log
import memory.job_id
import memory.lastJob_id
import room.getCreepOfRole
import screeps.api.Creep
import screeps.api.FIND_MY_SPAWNS
import screeps.api.RESOURCE_ENERGY

class WorkerCreep(private val creep: Creep):EcoCreep {
    private var job: Job? = creep.getJob()
    override fun performTask(): Boolean {
        log(LogLevel.DEBUG,"Preforming Task (${job?.jobType})","performTask",creep.name)

        if (job == null){ // idl creep give jobs
            job = creep.getJob()
        }

        var r: ActionOutcome = ActionOutcome.ERROR
        // Idle
        if (job == null || creep.memory.job_id == "") {
            if (creep.memory.job_id !== "") {
                log(LogLevel.ERROR,"job not found ${creep.memory.job_id}","performTask",creep.name) // should happen
                creep.memory.job_id = ""
            }
            log(LogLevel.INFO,"job not found or idle","performTask",creep.name)
            creep.say("Idle")


            //Idle help less creep
            if (creep.role() == CreepRole.CARRIER
                && creep.room.getCreepOfRole(CreepRole.MINER) == 0
                && creep.room.getCreepOfRole(CreepRole.WORKER) <= 1
                && creep.room.storage?.store?.getUsedCapacity(RESOURCE_ENERGY) ?: 0 < 300
            ) {
                r = creep.recycleHandler()
            } else {
                return false
            }


        } else {
            //Process task
            when (JobType.valueOf(job!!.jobType)) {
                JobType.HARVEST_SOURCE ->  r = creep.harvestHandler(job!!.target_id)
                JobType.DROP_OFF_ENERGY -> r = creep.transferHandler(job!!)
                JobType.UPGRADE_CONTROLLER -> r = creep.upgradeControllerHandler(job!!.target_id)
                JobType.BUILD -> r = creep.buildHandler(job!!.target_id)
                JobType.REPAIR -> r = creep.repairHandler(job!!.target_id)
                JobType.WITHDRAW -> r = creep.withdrawHandler(job!!)
                JobType.PICKUP -> r = creep.pickUpHandler(job!!)
                JobType.RENEW -> r = creep.renewHandler()
            }
        }



        // Handlee output
        log(LogLevel.DEBUG,"ActionOutcome $r","performTask",creep.name)
        when (r) {
            ActionOutcome.OK -> return true
            ActionOutcome.COMPLETED_ALREADY -> creep.completeJob(job!!) //TODO: can do something else this tick
            ActionOutcome.COMPLETED -> creep.completeJob(job!!)
            ActionOutcome.INVALID -> creep.completeJob(job!!) // task might of been completed by another creep
            else -> log(LogLevel.ERROR,"Unhandled result $r, for ${job!!.jobType}","performTask",creep.name)
        }


        if (creep.memory.job_id == ""
                && creep.memory.lastJob_id !== "Renew-${creep.name}"
                && !creep.canIBeBigger()
        ){
            if (creep.ticksToLive < 400) {
                creep.memory.job_id = "Renew-${creep.name}"
            }
        }

        return true
    }



}