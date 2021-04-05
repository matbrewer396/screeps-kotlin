package empire.eco

import screeps.api.BodyPartConstant
import screeps.api.*

fun buildBody(startingBody: List<BodyPartConstant>, addBody: List<BodyPartConstant>, startingEnergyAvailable: Int ) : MutableList<BodyPartConstant> {
    var body = mutableListOf<BodyPartConstant>()
    var energyAvailable = startingEnergyAvailable
    // Starting body
    startingBody.forEach { bodyPart ->
        body.add(bodyPart)
        energyAvailable -= BODYPART_COST[bodyPart]!!
    }
    // Find smallist size
    var minBody: Int = 10000
    var i: Int = 0

    addBody.forEach { if (minBody > BODYPART_COST[it]!!) {minBody = BODYPART_COST[it]!!} }

    // keep building to max
    while (energyAvailable >= minBody && i < 50) {
        addBody.forEach { bodyPart ->
            if (energyAvailable >= BODYPART_COST[bodyPart]!!) {
                energyAvailable -= BODYPART_COST[bodyPart]!!
                body.add(bodyPart)
                i+=1
            }
        }
    }


    return body
}