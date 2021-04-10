package creep

enum class CreepRole(val maxNumber: Int, val canRefileSpawn: Boolean, val maxBodySize: Int) {
    WORKER(5, true, 3350),
    MINER(2,true, 750),
    CARRIER(2, false, 2600)
}
