package com.github.m5rian

fun inputString(label: String): String {
    println(label)
    return readln()
}

fun doubleInput(label: String): Double {
    println(label)
    return readln().toDoubleOrNull() ?: return doubleInput(label)
}

fun intInput(label: String): Int {
    println(label)
    return readln().toIntOrNull() ?: return intInput(label)
}

private val yes = listOf("y", "yes")
private val no = listOf("n", "no")
fun booleanInput(label: String): Boolean {
    println(label)
    val res = readln().lowercase()
    return when (res) {
        in yes -> true
        in no  -> false
        else   -> booleanInput(label)
    }
}

fun anyInput(label: String) {
    println(label)
    readln()
}