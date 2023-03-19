package com.github.m5rian

import NeuronNetwork
import TrainingData
import com.github.m5rian.visualizer.Visualizer2d
import loaders.CustomDataLoader

suspend fun main() {
    val trainingSamples = CustomDataLoader("custom-data").loadSamples()
    //val neuronNetwork = NeuronNetwork.fromLayerSizes(2, 4, 2, 3, 2)
    val neuronNetwork = NeuronNetwork.fromLayerSizes(2, 3, 2)

    val visualizer = Visualizer2d(neuronNetwork, 0..1100, 0..800)
    visualizer.dataPoints(trainingSamples) {
        TrainingData(
            inputs = listOf(it.x, it.y),
            outputs = if (it.classification == 1.toByte()) listOf(1.0, 0.0) else listOf(0.0, 1.0)
        )
    }
    visualizer.open()
}