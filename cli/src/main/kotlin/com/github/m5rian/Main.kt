package com.github.m5rian

fun main() {
    println(
        """
        Wähle aus:
        (1) Load Data
        (2) Train Data
    """.trimIndent()
    )
    val action = readln()
    println(action)
}