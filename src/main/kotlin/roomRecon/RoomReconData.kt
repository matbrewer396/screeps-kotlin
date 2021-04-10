package roomRecon

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.api.structures.StructureKeeperLair

class RoomReconData(
    var lastUpdate: Int,
    val roomName: String,
    var contoller: StructureController,
    val sources: List<Source>,
    val hasKeeperLair: Boolean,
    var isHostile: Boolean
) {


}