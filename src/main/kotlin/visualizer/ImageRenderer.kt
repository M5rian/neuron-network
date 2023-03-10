package visualizer

import NeuronNetwork
import TrainingData
import getColour
import kotlinx.coroutines.*
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.ForkJoinPool
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ImageRenderer(
    private val xRange: IntRange,
    private val yRange: IntRange,
    private val neuronNetwork: NeuronNetwork,
) {
    private val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    val width = xRange.last - xRange.first
    val height = yRange.last - yRange.first
    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    fun getImage() = image

    suspend fun render() {
        renderDecisionBoundary()
        renderAxis()
    }

    private suspend fun renderDecisionBoundary() {
        val jobs = mutableListOf<Deferred<Unit>>()
        for (yPosition in 0 until height) {
            val yValue = (yRange.first + yPosition).toDouble()
            val yValueDrawnRight = yRange.last - yPosition - 1

            val job = coroutineScope.async {
                for (xPosition in 0 until width) {
                    val xValue = (xRange.first + xPosition).toDouble()

                    val inputs = neuronNetwork.normalize(listOf(xValue, yValue), listOf(width.toDouble(), height.toDouble()))
                    val outputs = neuronNetwork.predict(inputs)

                    image.setRGB(xPosition, yValueDrawnRight, getColour(outputs).rgb)
                }
            }
            jobs.add(job)
        }
        awaitAll(*jobs.toTypedArray())
    }

    private fun getGradient(outputs: List<Double>): Color {
        var alpha = outputs[0] - outputs[1]
        alpha = max(alpha, -1.0)
        alpha = min(alpha, 1.0)
        alpha = (1.0 + alpha) / 2.0
        val r = (alpha * 0xFF).toInt()
        val g = ((1 - alpha) * 0xFF).toInt()
        val b = 0
        return Color(r, g, b)
    }

    private fun renderAxis() {
        val graphics = image.createGraphics()
        graphics.color = Color.BLACK
        graphics.stroke = BasicStroke(2.0f)
        // Y Axis
        val xOffset = xRange.first
        if (xOffset < 0) { // Y-axis would be visible
            val xLevel = abs(xOffset)
            graphics.drawLine(xLevel, 0, xLevel, height)
        }
        // X Axis
        val yOffset = yRange.first
        if (yOffset < 0) { // X-axis would be visible
            val yLevel = height - abs(yOffset)
            graphics.drawLine(0, yLevel, width, yLevel)
        }
    }

    fun renderDataPoints(trainingData: List<TrainingData>) {
        val radius = 4
        val graphics = image.graphics
        trainingData.forEach {
            val x = it.inputs[0].roundToInt()
            val y = it.inputs[1].roundToInt()

            val relativeX = xRange.first * -1 + x
            val relativeY = height - (yRange.first * -1 + y)
            val colour = if (it.outputs[0] == 1.0) Color(0, 222, 96) else Color(255, 87, 112)
            graphics.color = colour
            graphics.fillOval(relativeX - radius, relativeY - radius, radius * 2, radius * 2)
        }
    }
}