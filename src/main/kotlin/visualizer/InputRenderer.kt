package visualizer

import Layer
import Neuron
import NeuronNetwork
import kotlinx.coroutines.*
import java.awt.Dimension
import java.awt.Panel
import java.util.concurrent.ForkJoinPool
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.roundToInt

class InputRenderer(
    private val visualizer: Visualizer2d,
    private val neuronNetwork: NeuronNetwork
) {
    private val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())

    data class NumberInputManager(val input: JTextField, val slider: JSlider, val inputListener: InputListener)

    val biasInputs = MutableList(neuronNetwork.layers.size) { layerIndex ->
        val layer = neuronNetwork.layers[layerIndex]
        val neurons = layer.neurons.map { neuron ->
            createNumberInput(neuron.bias) { neuron.bias = it }
        }
        neurons
    }
    val weightInputs = MutableList(neuronNetwork.layers.size) { layerIndex ->
        val layer = neuronNetwork.layers[layerIndex]
        val neuronsList = layer.neurons.map { neuron ->
            val weightsList = MutableList(neuron.weights.size) { weightIndex ->
                val input = createNumberInput(neuron.weights[weightIndex]) { neuron.weights[weightIndex] = it }
                input
            }
            weightsList
        }
        neuronsList
    }

    fun render(panel: JPanel) {
        neuronNetwork.layers.forEachIndexed { layerIndex, layer ->
            panel.renderLayer(layerIndex, layer)
        }
    }

    private fun JPanel.renderLayer(layerIndex: Int, layer: Layer) {
        add(JLabel("Layer $layerIndex"))
        layer.neurons.forEachIndexed { neuronIndex, neuron ->
            renderNeuron(layerIndex, neuronIndex, neuron)
        }
    }

    private fun JPanel.renderNeuron(layerIndex: Int, neuronIndex: Int, neuron: Neuron) {
        add(JLabel("Neuron $neuronIndex"))

        add(Panel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(JLabel("Bias"))
            val biasInputManager = biasInputs[layerIndex][neuronIndex]
            add(biasInputManager.input)
            add(biasInputManager.slider)
        })

        neuron.weights.forEachIndexed { weightIndex, weight ->
            renderWeights(layerIndex, neuronIndex, neuron, weightIndex, weight)
        }
    }

    private fun JPanel.renderWeights(layerIndex: Int, neuronIndex: Int, neuron: Neuron, weightIndex: Int, weight: Double) {
        add(Panel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JLabel("Weight $weightIndex"))
            val weightInputManager = weightInputs[layerIndex][neuronIndex][weightIndex]
            add(weightInputManager.input)
            add(weightInputManager.slider)
        })
    }

    private fun createNumberInput(defaultNumber: Double, updateFunction: (Double) -> Unit): NumberInputManager {
        var inputListener: InputListener
        val input = JTextField(defaultNumber.toString()).apply {
            minimumSize = Dimension(200, 35)

            inputListener = InputListener(this, updateFunction) {
                coroutineScope.launch { visualizer.rerender() }
            }
            document.addDocumentListener(inputListener)
        }

        val slider = JSlider(-3 * 10, 3 * 10, (defaultNumber * 10).roundToInt()).apply {
            var isAdjusting = false
            var rerenderJob: Job? = null
            addChangeListener {
                //Live Updating
                if (!isAdjusting) rerenderJob = coroutineScope.async {
                    while (true) visualizer.rerender()
                }

                isAdjusting = true
                val numberValue = value / 10.0

                // Update text input without dispatching an event
                inputListener.ignore = true
                input.text = numberValue.toString()
                // Update values in neuron network
                updateFunction.invoke(numberValue)

                // Final rerender
                if (!valueIsAdjusting) coroutineScope.launch {
                    isAdjusting = false
                    rerenderJob?.cancel()

                    inputListener.ignore = false
                    coroutineScope.launch { visualizer.rerender() }
                }
            }
        }

        return NumberInputManager(input, slider, inputListener)
    }

    class InputListener(
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