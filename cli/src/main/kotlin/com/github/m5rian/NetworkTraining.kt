package com.github.m5rian

import NeuronNetwork
import TrainingData
import com.github.m5rian.loader.Idx3UbyteLoader
import kotlinx.coroutines.*
import me.tongfei.progressbar.ProgressBar
import java.util.concurrent.ForkJoinPool
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

suspend fun train(network: NeuronNetwork) {
    val imageSet = Idx3UbyteLoader.loadImageSet("/mnist-database/train-images")
    val labelSet = Idx3UbyteLoader.loadLabelSet("/mnist-database/train-labels")

    val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    val createTrainingDataProgressBar = ProgressBar("Converting data", imageSet.count.toLong())

    val jobs = imageSet.images.slice(0 until 5000).mapIndexed { index, image ->
        coroutineScope.async {
            val inputs = image.pixels.map { it.toDouble() }

            val outputIndex = labelSet[index].toInt()
            val outputs = MutableList(10) { 0.0 }
            outputs[outputIndex] = 1.0

            createTrainingDataProgressBar.step()
            createTrainingDataProgressBar.refresh()
            TrainingData(inputs, outputs)
        }
    }
    val trainingData = awaitAll(*jobs.toTypedArray())
    createTrainingDataProgressBar.close()

    val maxValues = MutableList(imageSet.pixels) { UByte.MAX_VALUE.toDouble() }
    val normalizedData = network.normalizeSet(trainingData, maxValues)

    val learnRate = doubleInput("Set a learn rate:")
    val saveInterval = intInput("Set a saving interval (in minutes)")

    println(
        """
        Training data: ${normalizedData.size}
        Learn rate: $learnRate
        Save interval: every $saveInterval minutes
    """.trimIndent()
    )

    delay(500)
    println("Starting!")

    val learningSince = System.currentTimeMillis()
    var learnIteration = 0

    coroutineScope.launch {
        while (true) {
            network.learn(trainingData, learnRate)
            learnIteration++
        }
    }
    coroutineScope.launch { save(network, saveInterval) }
    /*
    while (true) {
        updateStats(network, trainingData, learnRate, learningSince, learnIteration)
    }*/


    while (true) {
        // Block Code
    }
}

suspend fun save(network: NeuronNetwork, saveInterval: Int) {
    while (true) {
        delay(saveInterval.minutes.inWholeMilliseconds)
        val filename = System.currentTimeMillis().toString()
        network.toFile("/$filename")
    }
}

fun updateStats(network: NeuronNetwork, trainingData: List<TrainingData>, learnRate: Double, learningSince: Long, learnIteration: Int) {
    val correct = network.test(trainingData)
    val percentage = correct / trainingData.size * 100.0

    val loss = network.averageCost(trainingData)

    val start = learningSince.milliseconds
    val timestamp = System.currentTimeMillis().milliseconds
    val duration = timestamp.minus(start)
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds - (minutes * 60)

    println(clearScreen)
    println(
        """ 
            Learning for = ${minutes}:${seconds}m
            Learn iteration = $learnIteration
            Learn Rate = $learnRate
            
            Correct = $correct / ${trainingData.size}
            Accuracy = $percentage%
            Loss = $loss
        """.trimIndent()
    )
}