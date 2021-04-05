package memory

import empire.PlanStructure
import job.Job
import screeps.api.CreepMemory
import screeps.api.Memory
import screeps.api.RoomMemory
import screeps.utils.memory.memory

var CreepMemory.role: String by memory { "" }       // Current role
var CreepMemory.task: String by memory { "" }       // Current task
var CreepMemory.type: String by memory { "" }       // eco or mitlery
var CreepMemory.homeRoom: String by memory { "" }   // Room it live in
var CreepMemory.job_id: String by memory{""}


var RoomMemory.jobs: Array<Job> by memory { arrayOf<Job>() }
var RoomMemory.plan: Array<PlanStructure> by memory { arrayOf<PlanStructure>() }
var RoomMemory.planNextCheck: Int by memory { 0 }
