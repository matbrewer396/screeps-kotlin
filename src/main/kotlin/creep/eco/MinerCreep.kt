package creep.eco

import RoomPosition.getNearByPositions
import creep.*
import job.Job
import job.JobType
import log.LogLevel
import log.log
import memory.job_id
import screeps.api.*

class MinerCreep(private val creep: Creep):EcoCreep {
    private var job: Job? = creep.getJob()
    override fun performTask(): Boolean {
        log(LogLevel.DEBUG,"Preforming Task (${job?.jobType})","performTask",creep.name)

        if (job == null){ job = creep.getJob() } // idl creep give jobs
        if (job == null || creep.memory.job_id == "") {
            if (creep.memory.job_id !== "") {
                log(LogLevel.ERROR,"job not found ${creep.memory.job_id}","performTask",creep.name) // should happen
                creep.memory.job_id == ""
            }
            log(LogLevel.DEBUG,"job not found or idle","performTask",creep.name)
            return true
        }
        if (JobType.valueOf(job!!.jobType) == JobType.MINING_STATION){
           if (creep.pos.x !== job!!.roomPos.x || creep.pos.y !== job!!.roomPos.y) {
               log(LogLevel.DEBUG,"Moving","performTask",creep.name)
               var r = creep.moveHandler(RoomPosition(job!!.roomPos.x,job!!.roomPos.y,job!!.roomPos.roomName))
               when (r) {
                   ActionOutcome.OK -> return true
                   ActionOutcome.COMPLETED_ALREADY -> {}
                   ActionOutcome.COMPLETED -> return true
                   else -> log(LogLevel.ERROR,"Unhandled result $r, for ${job!!.jobType}","performTask",creep.name)
               }

           }

            //Mine
            var source = creep.pos.findInRange(FIND_SOURCES,1)[0]
            var harvestResult = creep.harvest(source)
            log(LogLevel.DEBUG,"Mining ${source.id}, $harvestResult","performTask",creep.name)
            when (harvestResult)  {
                OK -> return true
                ERR_NOT_ENOUGH_RESOURCES -> return false
                ERR_NOT_IN_RANGE -> creep.moveHandler(RoomPosition(job!!.roomPos.x,job!!.roomPos.y,job!!.roomPos.roomName))
                else -> log(LogLevel.ERROR,"Unhandled harvesting result $harvestResult","performTask",creep.name)
            }
        }

        return true
    }
}