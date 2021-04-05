import empire.EmpireManager

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@Suppress("unused")
fun loop() {
    val cm = EmpireManager()
    cm.runEmpire()
}
