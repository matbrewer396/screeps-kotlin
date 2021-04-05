package creep.eco

import creep.PlayerCreep

interface EcoCreep: PlayerCreep {
    fun performTask(): Boolean
}