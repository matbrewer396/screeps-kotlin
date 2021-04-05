package log


enum class LogLevel(val priority: Int) {
    ALWAYS(0)
    ,ERROR(1)
    ,INFO(2)
    ,DEBUG(3)


}

fun log (lvl: LogLevel, msg: String, filterKey1: String, filterKey2: String) {
    var outputMsg = false
    if (LogLevel.ERROR.priority >= lvl.priority) {
        outputMsg = true
    } else if (config.logLevel.priority >= lvl.priority) {
        if (config.logFilterKey1 == "All") {
            if (config.logFilterKey2 == "All") {
                outputMsg = true
            } else if (filterKey2 == config.logFilterKey2) {
                outputMsg = true
            }
        } else if (filterKey1 == config.logFilterKey1) {
            if (config.logFilterKey2 == "All") {
                outputMsg = true
            } else if (filterKey2 == config.logFilterKey2) {
                outputMsg = true
            }
        }
    }

    if (outputMsg) {
        console.log("$lvl ($filterKey1 - $filterKey2 ) $msg")
    }
}