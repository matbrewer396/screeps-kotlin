package screepPrototype

import screeps.api.*
/*
* Return list of all near by pos
* */
fun RoomPosition.getNearByPositions(range: Int) :List<RoomPosition>{
    var positions = mutableListOf<RoomPosition>()
    var startX = this.x - range
    var startY = this.y - range

    var maxX = this.x + range
    var maxY = this.y + range
    if (maxX > 49) {maxX = 49}
    if (maxY > 49) {maxX = 49}

    for (iX in startX..maxX) {
        for (iY in startY..maxY) {
            if (iX == this.x && iY == this.y) { continue } // Starting location
            positions.add(RoomPosition(iX,iY,this.roomName))
        }
    }
    return positions
}

fun RoomPosition.isBuildable(): Boolean {
    return this.lookFor(LOOK_TERRAIN)?.get(0) ?: "" !== "wall"
}
fun RoomPosition.getAdjacentPositions(range: Int) :List<RoomPosition>{
    var positions = mutableListOf<RoomPosition>()

    for (r in 1..range) {
        positions.add(RoomPosition(x-r,y, roomName))
        positions.add(RoomPosition(x+r,y, roomName))
        positions.add(RoomPosition(x,y-r, roomName))
        positions.add(RoomPosition(x,y+r, roomName))
    }

    return positions
}