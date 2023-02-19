package visualizer

import Layer
import Neuron
import NeuronNetwork
import kotlinx.coroutines.*
import me.tongfei.progressbar.ProgressBar
import java.awt.Color
import java.awt.Dimension
import java.awt.Panel
import java.awt.image.BufferedImage
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.roundToInt

class Visualizer2d(
    private val neuronNetwork: NeuronNetwork,
    xRange: IntRange,
    yRange: IntRange
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    //private val zoomFactor = 1 / zoom

    private val width = xRange.last - xRange.first
    private val height = yRange.last - yRange.first

    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private lateinit var imageLabel: JLabel

    private val dataPoints = mutableListOf<DataPoint>()

    init {
        if (neuronNetwork.inputs != 2) throw IllegalStateException("Input layer must have 2 neurons")
        if (neuronNetwork.layers.last().neurons.size != 2) throw IllegalStateException("Output layer must have 2 neurons")
    }

    fun <T> dataPoints(trainingSamples: List<T>, function: (T) -> DataPoint) {
        dataPoints.addAll(trainingSamples.map(function))
    }

    private suspend fun renderImage(): BufferedImage {
        renderDecisionBoundary()
        //if (dataPoints.isNotEmpty()) renderDataPoints()

        return image
    }

    private suspend fun renderDecisionBoundary() {
        val progressBar = ProgressBar("render image", height.toLong())

        val jobs = mutableListOf<Deferred<Unit>>()
        for (y in 0 until height) {
            val job = coroutineScope.async {
                for (x in 0 until width) {
                    val inputs = listOf(x.toDouble(), y.toDouble())
                    val outputs = neuronNetwork.predict(inputs)

                    val colour = if (outputs[0] > outputs[1]) Color.GREEN else Color.RED
                    image.setRGB(x, y, colour.rgb)
                }
                progressBar.step()
                progressBar.refresh()
            }
            jobs.add(job)
        }
        awaitAll(*jobs.toTypedArray())
        progressBar.close()
    }

    private fun renderDataPoints() {
        val radius = 1
        val graphics = image.graphics
        graphics.color = Color.WHITE
        dataPoints.forEach {
            val x = it.x.roundToInt()
            val y = it.y.roundToInt()
            graphics.drawOval(x - radius, y - radius, radius * 2, radius * 2)
        }
    }

    suspend fun render() = JFrame().apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            // Visualization image
            imageLabel = JLabel(ImageIcon(renderImage()))
            add(imageLabel)
            // Bias and weight settings
            neuronNetwork.layers.forEachIndexed { layerIndex, layer ->
                renderLayer(layerIndex, layer)
            }
        })
        isVisible = true
    }

    private fun JPanel.renderLayer(layerIndex: Int, layer: Layer) {
        add(JLabel("Layer $layerIndex"))
        layer.neurons.forEachIndexed { neuronIndex, neuron ->
            renderNeuron(neuronIndex, neuron)
        }
    }

    private fun JPanel.renderNeuron(neuronIndex: Int, neuron: Neuron) {
        add(JLabel("Neuron $neuronIndex"))

        add(Panel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JLabel("Bias"))
            addNumberInput(neuron.bias) { neuron.bias = it }
        })

        neuron.weights.forEachIndexed { weightIndex, weight ->
            renderWeights(neuron, weightIndex, weight)
        }
    }

    private fun JPanel.renderWeights(neuron: Neuron, weightIndex: Int, weight: Double) {
        add(Panel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JLabel("Weight $weightIndex"))
            addNumberInput(weight) { neuron.weights[weightIndex] = it }
        })
    }

    private fun Panel.addNumberInput(defaultNumber: Double, updateFunction: (Double) -> Unit) {
        val rerenderFunction: () -> Unit = {
            coroutineScope.launch {
                imageLabel.icon = ImageIcon(renderImage())
                imageLabel.repaint()
            }
        }

        var inputListener: InputListener
        val input = JTextField(defaultNumber.toString()).apply {
            minimumSize = Dimension(200, 35)

            inputListener = InputListener(this, updateFunction, rerenderFunction)
            document.addDocumentListener(inputListener)
        }

        val slider = JSlider(-1 * 10, 1 * 10, 0).apply {
            addChangeListener {
                val numberValue = value / 10.0

                // Update text input without dispatching an event
                inputListener.ignore = true
                input.text = numberValue.toString()

                if (!valueIsAdjusting) {
                    coroutineScope.launch {
                        inputListener.ignore = false
                        updateFunction.invoke(numberValue)
                        imageLabel.icon = ImageIcon(renderImage())
                        imageLabel.repaint()
                    }
                }
            }
        }

        add(input)
        add(slider)
    }

    private class InputListener(
        private val textField: JTextField,
        private val updateFunction: (Double) -> Unit,
        private val rerenderFunction: () -> Unit
    ) : DocumentListener {
        var ignore = false

        override fun insertUpdate(e: DocumentEvent?) = update()
        override fun removeUpdate(e: DocumentEvent?) = update()
        override fun changedUpdate(e: DocumentEvent?) = update()
        private fun update() {
            val number = textField.text.toDoubleOrNull() ?: 0.0
            if (ignore) return

            updateFunction.invoke(number)
            rerenderFunction.invoke()
        }
    }

}