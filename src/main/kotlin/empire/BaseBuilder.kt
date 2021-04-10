package empire

import screepPrototype.*
import config.startBuildingRoadAtRCl
import log.*
import memory.*
import screeps.api.*
import screeps.utils.unsafe.jsObject

class BaseBuilder() {

    private var processedPos = mutableListOf<RoomPosition>()
    private var planStructures = mutableListOf<PlanStructure>()
    fun planMaker(room: Room) {

//        room.find(FIND_CONSTRUCTION_SITES).forEach {
//            it.remove()
//        }




        if (room.memory.plan.isEmpty()) {
            log(LogLevel.DEBUG, "Starting plan for room ${room.name}", "planMaker", "")

            // Build around starting spwan
            val mainSpawn = room.find(FIND_MY_SPAWNS)[0]
            val mainSpawnNearBy = mainSpawn.pos.getNearByPositions(2)?.filter { it.isBuildable() }

            mainSpawnNearBy.forEach { planStructure(it, STRUCTURE_ROAD, 1, room, false) }

            // Build Container for miner
            room.find(FIND_SOURCES).forEach { source ->
                val nearBy = source.pos.getAdjacentPositions(1).filter { it.isBuildable() }
                planStructure(nearBy[nearBy.size / 2], STRUCTURE_CONTAINER, 1, room, true)
                //build path to controller
                room.controller?.let {
                    PathFinder.search(source.pos, it.pos, jsObject {
                        this.plainCost = 0
                        this.swampCost = 0 // building road so doesnt matter
                    }).path.forEach {
                        planStructure(it, STRUCTURE_ROAD, 1, room, false)
                    }
                }
                // build path to main spawm
                PathFinder.search(source.pos, mainSpawn.pos, jsObject {
                    this.plainCost = 0
                    this.swampCost = 0 // building road so doesnt matter
                }).path.forEach {
                    planStructure(mainSpawn.pos, STRUCTURE_ROAD, 1, room, false)
                }

            }

            mainSpawnNearBy.forEach { mainSpawnNearByPos ->
                mainSpawnNearByPos.getNearByPositions(1)?.filter {
                    it.isBuildable()
                            && processedPos.none { p -> p.x == it.x && p.y == it.y }

                }.forEach { buildSites.add(it) }
            }
            processSpwanPos(room)
            room.memory.plan = planStructures.toTypedArray()
        } else {
            log(LogLevel.DEBUG, "exiting plan for room ${room.name}", "planMaker", "")
            var plan = room.memory.plan
            plan.forEach {
                if (room.lookForAt(LOOK_STRUCTURES, it.pos.x,it.pos.y)?.isNotEmpty() == true) {return@forEach}
                if (room.lookForAt(LOOK_CONSTRUCTION_SITES, it.pos.x,it.pos.y)?.isNotEmpty() == true) {return@forEach}
                planStructure(it.pos,it.structureConstant,it.rcl,room, false)
                if (it.rcl <= room.controller?.level ?: -1) {
                    var r = room.createConstructionSite(it.pos.x,it.pos.y,it.structureConstant)
                    when (r) {
                        OK ->  log(LogLevel.DEBUG, "New site created ${it.structureConstant} (${it.pos.x},${it.pos.y})", "planMaker", "")
                        ERR_RCL_NOT_ENOUGH -> {}
                        else ->  log(LogLevel.ERROR, "New site fail to create ${r} - ${it.structureConstant} (${it.pos.x},${it.pos.y})", "planMaker", "")

                    }

                }
           }
        }

        room.memory.planNextCheck = Game.time + 50
    }

    private val buildSites = mutableListOf<RoomPosition>()
    private fun processSpwanPos(room: Room) {
        var i = 500
        while (buildSites.size > 0 && i > 0) {
            val site = buildSites.removeFirst() // get next in queue
            if (processedPos.any { p -> p.x == site.x && p.y == site.y }) {
                continue
            } // check its not already process
            i -= 1 // safety
            var nextObject: BuildableStructureConstant = getNextStructure() ?: return

            // work out RCL should be built at
            val countOfPlanned = planStructures.filter { it.structureConstant == nextObject }.size
            +room.find(FIND_MY_STRUCTURES).filter { it.structureType == nextObject }.size
            var rcl: Int = 8

            for (j in 1..8) {
                if (CONTROLLER_STRUCTURES[nextObject]?.get(j) >= countOfPlanned) {
                    rcl = j
                    break
                }
            }



            planStructure(site, nextObject, rcl, room, false) // add structure
            // Build roads nearby
            site.getNearByPositions(1).filter { it.isBuildable() }.forEach { roadSite ->
                if (processedPos.any { p -> p.x == roadSite.x && p.y == roadSite.y }) {
                    return@forEach
                } // already been process
                planStructure(roadSite, STRUCTURE_ROAD, rcl, room, false) // create road

                roadSite.getNearByPositions(1).filter { it.isBuildable() }.forEach roadSite@{ nextSite ->
                    if (processedPos.any { p -> p.x == nextSite.x && p.y == nextSite.y }) {
                        return@roadSite
                    }
                    buildSites.add(nextSite)
                }

            }
        }
    }

    private fun getNextStructure(): BuildableStructureConstant? {
        if (isEnoughStructure(STRUCTURE_STORAGE, 0, 8)) {
            return STRUCTURE_STORAGE
        }
        if (isEnoughStructure(STRUCTURE_SPAWN, 1, 8)) {
            return STRUCTURE_SPAWN
        }
        // speated out tower
        for (i in 1..8) {
            if (isEnoughStructure(STRUCTURE_EXTENSION, 0, i)) {
                return STRUCTURE_EXTENSION
            }
            if (isEnoughStructure(STRUCTURE_TOWER, 0, i)) {
                return STRUCTURE_TOWER
            }
        }
        return null
    }

    private fun isEnoughStructure(b: BuildableStructureConstant, bias: Int, lvl: Int): Boolean {
        return CONTROLLER_STRUCTURES[b]?.get(lvl) > planStructures.filter { it.structureConstant == b }.size + bias
    }


    private fun planStructure(
        pos: RoomPosition,
        structureConstant: BuildableStructureConstant,
        rcl: Int,
        room: Room,
        surroundWithRoads: Boolean
    ) {
        var saveRCL = rcl
        if (processedPos.any { p -> p.x == pos.x && p.y == pos.y }) {
            return
        }
        // dont spam roads till late came
        if (structureConstant == STRUCTURE_ROAD && rcl < startBuildingRoadAtRCl) {
            saveRCL = startBuildingRoadAtRCl
        }

        planStructures.add(PlanStructure(pos, structureConstant, saveRCL))
        processedPos.add(pos)

        room.visual.circle(pos, jsObject<RoomVisual.CircleStyle> {
            this.fill = "transparent"
            this.radius = 0.45
            this.lineStyle = (if (rcl <= room.controller?.level ?: 0) {
                null
            } else {
                "dashed"
            }).unsafeCast<LineStyleConstant>()
            this.stroke = when (structureConstant) {
                STRUCTURE_ROAD -> "navy"
                STRUCTURE_CONTAINER -> "lime"
                STRUCTURE_EXTENSION -> "aqua"
                STRUCTURE_TOWER -> "olive"
                STRUCTURE_STORAGE -> "yellow"
                STRUCTURE_SPAWN -> "pink"
                else -> "red"
            }
        })

        if (surroundWithRoads) {
            pos.getNearByPositions(1).filter { it.isBuildable() }.forEach {
                planStructure(it, STRUCTURE_ROAD, rcl, room, false)
            }
        }

    }
}


data class PlanStructure(
    var pos: RoomPosition, var structureConstant: BuildableStructureConstant, var rcl: Int
)
