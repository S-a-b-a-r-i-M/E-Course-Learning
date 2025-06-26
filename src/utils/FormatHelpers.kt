package utils

import kotlin.enums.EnumEntries

fun formatDurationMinutes(duration: Int) =
    if (duration > 60)
        "${(duration / 60)}h ${(duration % 60)}m"
    else
        "$duration m"

fun String.capitalize(): String = this[0].uppercase() + this.substring(1).lowercase()

// Extension function on EnumEntries
fun <T: Enum<T>> EnumEntries<T>.fromString(inputValue: String, default: T): T {
    return find { it.name == inputValue.trim().uppercase() } ?: run {
        println("given value is not valid.Hence, assigning '${default.name.capitalize()}' by default")
        default
    }
}