package utils

fun getListInput(prompt: String, delimiter: String): List<String> {
    print(prompt)
    val input = readln().trim()
    return if (input.isNotEmpty())
        input.split(delimiter).map { it.trim() }.filter { it.isNotEmpty() }
    else
        emptyList()
}