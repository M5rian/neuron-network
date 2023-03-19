package com.github.m5rian

import NeuronNetwork


fun chooseNetwork(): NeuronNetwork {
    println(
        """
        Choose:
        (1) Create network
        (2) Load network
    """.trimIndent()
    )

    val action = readln().toIntOrNull() ?: return chooseNetwork()
    return when (action) {
        1    -> createNetwork()
        2    -> loadNetwork()
        else -> chooseNetwork()
    }
}

private fun createNetwork(): NeuronNetwork {
    val layerSizes = getLayerSizes()

    val network = NeuronNetwork.fromLayerSizes(*layerSizes.toIntArray())
    println("Created network!")
    return network
}

fun getLayerSizes(): List<Int> {
    println("Comma separated layer sizes (including input layer)")
    val layerSizes = readln().split(",").mapNotNull { it.toIntOrNull() }
    confirmLayers(layerSizes)
    return layerSizes
}

fun confirmLayers(layerSizes: List<Int>) {
    val inputLayer = layerSizes.first()
    val outputLayer = layerSizes.last()
    val hiddenLayerCount = layerSizes.size - 2

    val confirmed = booleanInput(
        """
            Your layer sizes are ${layerSizes.joinToString(",")}.
            Input layer = $inputLayer neurons
            Output Layer $outputLayer neurons
            Amount of hidden layers = $hiddenLayerCount layers

            Ok? (Y,N)
        """.trimIndent()
    )

    if (!confirmed) getLayerSizes()
}

private fun loadNetwork(): NeuronNetwork {
    return NeuronNetwork.fromLayerSizes(2, 2, 2)
}