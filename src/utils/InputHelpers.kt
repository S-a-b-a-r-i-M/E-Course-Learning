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

fun selectFromOption(options: Map<Int, String>): Int {
    while (true) {
        println("\nOption to choose ⬇️")
        options.forEach { (key, value) ->
            println("$key -> $value")
        }
        print("Enter your option: ")
        val input = readln().toIntOrNull()

        if (input != null && options.keys.contains(input)) return input

        println("Invalid input...Try again")
    }
}