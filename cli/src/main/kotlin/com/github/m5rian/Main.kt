package com.github.m5rian

import NeuronNetwork


var clearScreen = ""


data class Num(val int: Int)

suspend fun main() {
    repeat(50) { clearScreen += "\r\n" }

    val network = chooseNetwork()
    networkActions(network)
}

private suspend fun networkActions(network: NeuronNetwork) {
    println(clearScreen)
    println(
        """
        What to do next?
        (1) Predict
        (2) Train
    """.trimIndent()
    )
    val action = readln().toIntOrNull() ?: return networkActions(network)
    return when (action) {
        1    -> {}
        2    -> train(network)
        else -> networkActions(network)
    }
}
