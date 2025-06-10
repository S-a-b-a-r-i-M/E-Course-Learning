package utils

fun formatDurationMinutes(duration: Int) =
    if (duration > 60)
        "${(duration / 60)}h ${(duration % 60)}m"
    else
        "$duration m"