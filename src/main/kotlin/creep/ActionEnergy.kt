package creep

import job.Job
import log.LogLevel
import log.log
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureController

/*
* Handler harvesting a source
* */
fun Creep.harvestHandler(sourceId: String) :ActionOutcome {
    //say("Harvesting $sourceId")
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
fun Creep.transferHandler(targetId: String, resource: ResourceConstant) :ActionOutcome {
    if (this.store.getUsedCapacity(resource) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: StoreOwner = Game.getObjectById<StoreOwner>(targetId) ?: return ActionOutcome.INVALID
    if (target.store.getFreeCapacity(resource)==0)  { return ActionOutcome.COMPLETED_ALREADY }

    log(LogLevel.DEBUG,"transfer to ${target.toString()}","transferHandler",name)

    var r = transfer(target, resource)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled transfer result $r","transferHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.upgradeControllerHandler(targetId: String) :ActionOutcome {
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
    var resource: ResourceConstant = job.resource ?: RESOURCE_ENERGY

    if (this.store.getFreeCapacity(resource) == 0) { return ActionOutcome.COMPLETED_ALREADY }
    var target: StoreOwner = Game.getObjectById<StoreOwner>(job.target_id) ?: return ActionOutcome.INVALID
    if (target.store.getUsedCapacity(resource)==0)  { return ActionOutcome.COMPLETED_ALREADY }

    log(LogLevel.DEBUG,"withdraw to ${target.toString()}","withdrawHandler",name)

    var r = withdraw(target, resource)
    when (r)  {
        OK -> return ActionOutcome.OK
        ERR_NOT_IN_RANGE -> return moveHandler(target.pos)
        else -> log(LogLevel.ERROR,"Unhandled withdraw result $r","withdrawHandler",name)
    }
    return ActionOutcome.ERROR
}

fun Creep.pickUpHandler(job: Job) :ActionOutcome {
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