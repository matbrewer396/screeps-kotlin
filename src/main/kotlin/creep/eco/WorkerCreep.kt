package creep.eco


import creep.*
import job.Job
import job.JobType
import log.LogLevel
import log.log
import memory.job_id
import screeps.api.Creep

class WorkerCreep(private val creep: Creep):EcoCreep {
    private var startingTask: CreepTask = creep.getTask()
    private var job: Job? = creep.getJob()

    override fun performTask(): Boolean {
        log(LogLevel.DEBUG,"Preforming Task (${job?.jobType})","performTask",creep.name)

        if (job == null || creep.memory.job_id == "") {
            if (startingTask !== CreepTask.IDLE) {
                log(LogLevel.ERROR,"job null but task is not idle","performTask",creep.name) // should happen
                creep.setTask(CreepTask.IDLE)
            }
            log(LogLevel.DEBUG,"job not found or idle","performTask",creep.name)
            return true
        }
        var r: ActionOutcome = ActionOutcome.ERROR
        when (JobType.valueOf(job!!.jobType)) {
            JobType.HARVEST_SOURCE ->  r = creep.harvestHandler(job!!.target_id)
            JobType.DROP_OFF_ENERGY -> r = creep.transferHandler(job!!.target_id, job!!.resource!!)
            JobType.UPGRADE_CONTROLLER -> r = creep.upgradeControllerHandler(job!!.target_id)
            JobType.BUILD -> r = creep.buildHandler(job!!.target_id)
        }


        log(LogLevel.DEBUG,"ActionOutcome $r","performTask",creep.name)
        when (r) {
            ActionOutcome.OK -> return true
            ActionOutcome.COMPLETED -> creep.completeJob()
            else -> log(LogLevel.ERROR,"Unhandled result $r, for ${job!!.jobType}","performTask",creep.name)
        }




        //var outcome = creep.HarvestHandler()

        return true
    }



}