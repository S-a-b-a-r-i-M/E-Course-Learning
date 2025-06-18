package config

enum class LogLevel(val colorCode: String) {
    INFO("\u001B[32m"), // GREEN
    WARNING("\u001B[33m"), // YELLOW
    EXCEPTION("\u001B[31m") // RED
}

fun log(message: String, level: LogLevel) {
    println("${level.colorCode} message")
}