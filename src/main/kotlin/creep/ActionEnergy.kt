package creep

import job.Job
import job.SubJobType
import log.LogLevel
import log.log
import memory.lastWithDrawStorageAt
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.utils.toMap

/*
* Handler harvesting a source
* */
fun Creep.harvestHandler(sourceId: String) :ActionOutcome {
    say("Harvesting")
    log(LogLevel.DEBUG,"Harvesting $sourceId","performTask",name)
    if (this.store.getFreeCapacity(RESOURCE_ENERGY) == 0) {
        return ActionOutcome.COMPLETED_ALREADY
    }
    var targetMaybe: Source? = Game.getObjectById<Source>(sourceId) ?: return ActionOutcome.INVALID
    var target: Source = targetMaybe as Source
    var r = harvest(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_ENOUGH_RESOURCES -> return ActionOutcome.COMPLETED
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled harvesting result $r","harvestHandler",name)
    }
    return ActionOutcome.ERROR
}
/*
* T
* */
fun Creep.transferHandler(job: Job) :ActionOutcome {
    say("Transferring")
    var resource: ResourceConstant = job.resource ?: RESOURCE_ENERGY
    var target: StoreOwner = Game.getObjectById<StoreOwner>(job.target_id) ?: return ActionOutcome.INVALID

    if (job.structureType == STRUCTURE_STORAGE) {
        if (this.store.getUsedCapacity() == 0) { return ActionOutcome.COMPLETED_ALREADY }
        if (target.store.getFreeCapacity()==0)  { return ActionOutcome.COMPLETED_ALREADY }
        // DRop all resources
        resource = store?.toMap().keys.first()
    } else {
        if (this.store.getUsedCapacity(resource) == 0) { return ActionOutcome.COMPLETED_ALREADY }
        if (target.store.getFreeCapacity(resource)==0)  { return ActionOutcome.COMPLETED_ALREADY }
    }



    log(LogLevel.DEBUG,"transfer to ${target.toString()}","transferHandler",name)
    var r: ScreepsReturnCode = transfer(target, resource)
    when (r)  {
        OK -> return if (job.structureType == STRUCTURE_TOWER) {
            ActionOutcome.COMPLETED
        } else {
            ActionOutcome.OK
        }
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled transfer result $r","transferHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.upgradeControllerHandler(targetId: String) :ActionOutcome {
    say("Upgrading")
    if (this.store.getUsedCapacity(RESOURCE_ENERGY) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: StructureController = Game.getObjectById(targetId) ?: return ActionOutcome.INVALID

    log(LogLevel.DEBUG,"transfer to ${target.toString()}","transferHandler",name)

    var r = upgradeController(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled upgradeController result $r","transferHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.buildHandler(targetId: String) :ActionOutcome {
    say("Building")
    if (this.store.getUsedCapacity(RESOURCE_ENERGY) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: ConstructionSite = Game.getObjectById(targetId) ?: return ActionOutcome.INVALID
    log(LogLevel.DEBUG,"building ${target.toString()}","buildHandler",name)
    var r = build(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        ERR_NOT_ENOUGH_RESOURCES -> return ActionOutcome.COMPLETED
        ERR_INVALID_TARGET -> return ActionOutcome.INVALID
        ERR_NO_BODYPART -> return ActionOutcome.INVALID
        else -> log(LogLevel.ERROR,"Unhandled buildHandler result $r","buildHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.repairHandler(targetId: String) :ActionOutcome {
    say("Repairing")
    if (this.store.getUsedCapacity(RESOURCE_ENERGY) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: Structure = Game.getObjectById(targetId) ?: return ActionOutcome.INVALID
    if (target.hits == target.hitsMax) { return ActionOutcome.COMPLETED_ALREADY }
    log(LogLevel.DEBUG,"Repairing ${target.toString()}","repairHandler",name)
    var r = repair(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        ERR_NOT_ENOUGH_RESOURCES -> return ActionOutcome.COMPLETED
        ERR_INVALID_TARGET -> return ActionOutcome.INVALID
        ERR_NO_BODYPART -> return ActionOutcome.INVALID
        else -> log(LogLevel.ERROR,"Unhandled repairHandler result $r","repairHandler",name)
    }
    return ActionOutcome.ERROR
}


fun Creep.moveHandler(target: RoomPosition) :ActionOutcome {
    var r = moveTo(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_TIRED -> return ActionOutcome.OK
        else -> log(LogLevel.ERROR,"Unhandled movement result $r","moveHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.withdrawHandler(job: Job) :ActionOutcome {
    say("Withdrawing")
    var resource: ResourceConstant = job.resource ?: RESOURCE_ENERGY

    if (this.store.getFreeCapacity(resource) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: StoreOwner = Game.getObjectById<StoreOwner>(job.target_id) ?: return ActionOutcome.INVALID
    if (target.store.getUsedCapacity(resource)==0)  { return ActionOutcome.COMPLETED_ALREADY }

    log(LogLevel.DEBUG,"withdraw to ${target.toString()}","withdrawHandler",name)
    if (job.subJobType == SubJobType.STRUCTURE_STORAGE) {
        memory.lastWithDrawStorageAt = Game.time
    }

    var r = withdraw(target, resource)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled withdraw result $r","withdrawHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.pickUpHandler(job: Job) :ActionOutcome {
    say("Picking Up")
    var resource: ResourceConstant = job.resource ?: RESOURCE_ENERGY
    if (this.store.getFreeCapacity(resource) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: Resource = Game.getObjectById<Resource>(job.target_id) ?: return ActionOutcome.INVALID
    if (target.amount==0)  { return ActionOutcome.COMPLETED_ALREADY }

    log(LogLevel.DEBUG,"pickup to ${target.toString()}","pickUpHandler",name)

    var r = pickup(target)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled pickup result $r","pickUpHandler",name)
    }
    return ActionOutcome.ERROR
}


fun Creep.recycleHandler() :ActionOutcome {
    say("Recycling")
    var r = room.find(FIND_MY_SPAWNS)[0].recycleCreep(this)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(room.find(FIND_MY_SPAWNS)[0].pos)
        else -> log(LogLevel.ERROR,"Unhandled pickup result $r","pickUpHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.renewHandler() :ActionOutcome {
    say("Renewing")
    if (this.ticksToLive > 1450 ) return ActionOutcome.COMPLETED_ALREADY
    val spawn = room.find(FIND_MY_SPAWNS).firstOrNull { it.spawning == null
            && it.store.getUsedCapacity(RESOURCE_ENERGY) > 100 // doesnt likve self erget of spwan
        } ?: return ActionOutcome.INVALID
    var r = spawn.renewCreep(this)
    when (r)  {
        OK -> return if (this.ticksToLive > 1450 ) ActionOutcome.COMPLETED else ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(room.find(FIND_MY_SPAWNS)[0].pos)
        ERR_NOT_ENOUGH_RESOURCES -> return ActionOutcome.INVALID //TODO transfers
        else -> log(LogLevel.ERROR,"Unhandled pickup result $r","pickUpHandler",name)
    }
    return ActionOutcome.ERROR
}