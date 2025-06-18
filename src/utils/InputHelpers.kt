package utils

import core.course.schemas.ResourceStatus
import core.course.services.capitalize

fun getListInput(prompt: String, delimiter: String): List<String> {
    print(prompt)
    val input = readln().trim()
    return if (input.isNotEmpty())
        input.split(delimiter).map { it.trim() }.filter { it.isNotEmpty() }
    else
        emptyList()
}

fun getYesOrNo(prompt: String): Boolean {
    print(prompt)
    val input = readln().trim().lowercase()
    return input == "y" || input == "yes"
}

fun selectResourceStatus(onSelected: (ResourceStatus) -> Unit) {
    println("Enter status (${ResourceStatus.entries.joinToString(", ") {it.name.capitalize()}}")
    val input = readln().trim()
    if (input.isNotEmpty())
        onSelected(ResourceStatus.getFromStrValue(input))
    else
        println("Status unchanged")
}
