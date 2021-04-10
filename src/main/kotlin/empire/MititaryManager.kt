package empire

import room.isUnderAttck
import screeps.api.*
import screeps.api.structures.StructureTower
import screeps.utils.toMap

class MititaryManager {
    fun run() {

        Game.rooms.toMap().forEach { m ->
            if (!m.value.isUnderAttck()) {return}
            m.value.find(FIND_MY_STRUCTURES)
                .filter { it.structureType == STRUCTURE_TOWER }
                .unsafeCast<List<StructureTower>>()
                .forEach {
                    it.pos.findClosestByRange(FIND_HOSTILE_CREEPS)?.let { it1 -> it.attack(it1) }
                }

        }

    }
}