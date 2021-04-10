package job

import screeps.api.structures.StructureTower

fun notTowerSuitable (job: Job, tower: StructureTower) :Boolean{
    return false
}

fun validateTowerRepair (job: Job, tower: StructureTower) :Boolean{
    return true
}