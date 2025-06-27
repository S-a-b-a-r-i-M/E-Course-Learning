package config

enum class LogLevel(val colorCode: String) {
    INFO("\u001B[32m"), // GREEN
    WARNING("\u001B[33m"), // YELLOW
    EXCEPTION("\u001B[31m") // RED
}

fun logInfo(message: String, level: LogLevel, fileName: String = "") {
//    println("${level.colorCode}${if (fileName.isEmpty()) "" else "$fileName: "}$message\u001B[0m") // "\u001B[0m" -- code for reset the color
    println("${level.colorCode}$message\u001B[0m")
}