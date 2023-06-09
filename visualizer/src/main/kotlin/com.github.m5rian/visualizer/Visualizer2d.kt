package com.github.m5rian.visualizer

import NeuronNetwork
import TrainingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.ForkJoinPool
import javax.swing.*
import kotlin.time.Duration.Companion.milliseconds

class Visualizer2d(
    private val neuronNetwork: NeuronNetwork,
    xRange: IntRange,
    yRange: IntRange
) {

    init {
        if (neuronNetwork.inputs != 2) throw IllegalStateException("Input layer must have 2 neurons")
        if (neuronNetwork.layers.last().neurons.size != 2) throw IllegalStateException("Output layer must have 2 neurons")
    }

    private val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    private val imageRenderer = ImageRenderer(xRange, yRange, neuronNetwork)
    private val inputRenderer = InputRenderer(this, neuronNetwork)

    private val trainingData = mutableListOf<TrainingData>()
    private val normalizedTrainingData: List<TrainingData>
        get() = neuronNetwork.normalizeSet(
            trainingData, listOf(imageRenderer.width.toDouble(), imageRenderer.height.toDouble())
        )

    private val updateButton = JButton("Update!").apply { initializeUpdateButton(this) }
    private val learnOnceButton = JButton("Learn once").apply { initializeLearnOnceButton(this) }
    private val learnButton = JButton("Learn!").apply { initializeLearnButton(this) }
    private var trainingSince: Long? = null
    private val trainingTimeLabel = JLabel("0:0m")
    private val lossLabel = JLabel("Loss = ?")
    private val correctLabel = JLabel("Correct = ?")
    private lateinit var imageLabel: JLabel

    fun <T> dataPoints(trainingSamples: List<T>, function: (T) -> TrainingData) {
        trainingData.addAll(trainingSamples.map(function))
    }

    suspend fun open() = JFrame().apply {
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(updateButton)
            add(learnOnceButton)
            add(learnButton)
            add(trainingTimeLabel)

            // Visualization image
            val image = imageRenderer.apply {
                render()
                renderDataPoints(trainingData)
            }.getImage()
            imageLabel = JLabel(ImageIcon(image))
            add(imageLabel)

            add(lossLabel.apply {
                val loss = neuronNetwork.averageCost(normalizedTrainingData)
                text = "Loss = $loss"
            })
            add(correctLabel.apply {
                val correctSamples = neuronNetwork.test(normalizedTrainingData)
                text = "Correct = $correctSamples / ${trainingData.size}"
            })

            // Bias and weight settings
            inputRenderer.render(this)
        }

        val scrollPane = JScrollPane(panel)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        add(scrollPane)
        isVisible = true
    }

    suspend fun rerender() {
        val image = imageRenderer.apply {
            render()
            renderDataPoints(trainingData)
        }.getImage()
        imageLabel.icon = ImageIcon(image)
        imageLabel.repaint()

        val start = trainingSince?.milliseconds
        val timestamp = System.currentTimeMillis().milliseconds
        val duration = start?.let { timestamp.minus(it) }
        val minutes = duration?.inWholeMinutes ?: 0
        val seconds = (duration?.inWholeSeconds ?: 0) - (minutes * 60)
        trainingTimeLabel.text = "${minutes}:${seconds}m"

        val loss = neuronNetwork.averageCost(normalizedTrainingData)
        lossLabel.text = "Loss = $loss"

        val correctSamples = neuronNetwork.test(normalizedTrainingData)
        correctLabel.text = "Correct = $correctSamples / ${trainingData.size}"
    }

    private fun initializeLearnOnceButton(button: JButton) {
        button.addActionListener {
            coroutineScope.launch {
                neuronNetwork.learn(normalizedTrainingData, 0.001)
                rerender()
            }
        }
    }

    private fun initializeLearnButton(button: JButton) {
        var learning = false

        var learningJob: Job? = null
        var rerenderJob: Job? = null

        fun startLearning() {
            trainingSince = System.currentTimeMillis()
            button.text = "Stop!"

            learningJob = coroutineScope.launch {
                while (true) {
                    neuronNetwork.learn(normalizedTrainingData, 1.5)
                }
            }
            rerenderJob = coroutineScope.launch {
                while (true) {
                    rerender()
                    /*
                    forEachWeight { layerIndex, neuronIndex, weightIndex, weight ->
                        val inputManager = inputRenderer.weightInputs[layerIndex][neuronIndex][weightIndex]
                        inputManager.input.text = weight.toString()
                       // inputManager.slider.value = 0
                    }*/
                }
            }
        }

        fun stopLearning() {
            trainingSince = null
            button.text = "Learn!"
            learningJob?.cancel()
            rerenderJob?.cancel()
        }

        button.addActionListener {
            if (learning) stopLearning()
            else startLearning()

            learning = !learning
        }
    }

    private fun initializeUpdateButton(button: JButton) {
        button.addActionListener {
            coroutineScope.launch { rerender() }
        }
    }

    private fun forEachWeight(function: (layer: Int, neuron: Int, weight: Int, Double) -> Unit) {
        for (layer in neuronNetwork.layers.indices) {
            for (neuron in neuronNetwork.layers[layer].neurons.indices) {
                for (weight in neuronNetwork.layers[layer].neurons[neuron].weights.indices) {
                    val weightValue = neuronNetwork.layers[layer].neurons[neuron].weights[weight]
                    function.invoke(layer, neuron, weight, weightValue)
                }
            }
        }
    }

}