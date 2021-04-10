package config
import log.LogLevel

val logLevel: LogLevel = LogLevel.INFO
const val logFilterKey1: String = "All" // "All"
const val logFilterKey2: String = "All" // "All"

// Planner
const val startBuildingRoadAtRCl: Int = 2
const val forcePlanOnEveryTick: Boolean = false
// Planner - Spawn
const val roadRangeBuildAroundSpawn: Int = 2
const val roadAroundMainSpawnStartAtRCL: Int = 3
// Planner - Minder
const val containerMinerAtRCL: Int = 2
const val containerMinerBuildRoadAtRCL: Int = 1
//Defence
const val buildSurroundingRampartAtRCL: Int = 5


