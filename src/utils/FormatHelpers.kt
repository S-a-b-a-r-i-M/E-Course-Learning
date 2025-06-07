package utils

fun formatDurationMinutes(duration: Float) = "${(duration / 60).toInt()}h ${(duration % 60).toInt()}m"