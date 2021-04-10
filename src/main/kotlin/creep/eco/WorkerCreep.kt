package creep.eco


import creep.*
import job.Job
import job.JobType
import log.LogLevel
import log.log
import memory.job_id
import screeps.api.Creep

class WorkerCreep(private val creep: Creep):EcoCreep {
    private var job: Job? = creep.getJob()
    override fun performTask(): Boolean {
        log(LogLevel.DEBUG,"Preforming Task (${job?.jobType})","performTask",creep.name)

        if (job == null){ // idl creep give jobs
            job = creep.getJob()
        }

        if (job == null || creep.memory.job_id == "") {
            if (creep.memory.job_id !== "") {
                log(LogLevel.ERROR,"job not found ${creep.memory.job_id}","performTask",creep.name) // should happen
                creep.memory.job_id = ""
            }
            log(LogLevel.DEBUG,"job not found or idle","performTask",creep.name)
            return false
        }
        var r: ActionOutcome = ActionOutcome.ERROR
        when (JobType.valueOf(job!!.jobType)) {
            JobType.HARVEST_SOURCE ->  r = creep.harvestHandler(job!!.target_id)
            JobType.DROP_OFF_ENERGY -> r = creep.transferHandler(job!!)
            JobType.UPGRADE_CONTROLLER -> r = creep.upgradeControllerHandler(job!!.target_id)
            JobType.BUILD -> r = creep.buildHandler(job!!.target_id)
            JobType.REPAIR -> r = creep.repairHandler(job!!.target_id)
            JobType.WITHDRAW -> r = creep.withdrawHandler(job!!)
            JobType.PICKUP -> r = creep.pickUpHandler(job!!)
        }


        log(LogLevel.DEBUG,"ActionOutcome $r","performTask",creep.name)
        when (r) {
            ActionOutcome.OK -> return true
            ActionOutcome.COMPLETED_ALREADY -> creep.completeJob(job!!) //TODO: can do something else this tick
            ActionOutcome.COMPLETED -> creep.completeJob(job!!)
            ActionOutcome.INVALID -> creep.completeJob(job!!) // task might of been completed by another creep
            else -> log(LogLevel.ERROR,"Unhandled result $r, for ${job!!.jobType}","performTask",creep.name)
        }




        //var outcome = creep.HarvestHandler()

        return true
    }



}