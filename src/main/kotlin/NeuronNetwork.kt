/**
 * @property inputs
 * @property layers All layers except the first one.
 */
class NeuronNetwork(
    val inputs: Int,
    val layers: List<Layer>
) {

    companion object {
        /**
         * @param layerSizes Amount of neurons in each layer.
         * @return Creates a new neuron network with the dedicated layers.
         */
        fun fromLayerSizes(vararg layerSizes: Int): NeuronNetwork {
            val layers = mutableListOf<Layer>()
            // Loop through each layer except input layer
            for (i in 1 until layerSizes.size) {
                val neuronCount = layerSizes[i]
                val neurons = Array(neuronCount) {
                    // Get amount of neurons in previous layer
                    // The previous neurons will be fet to the current layer
                    val incomingNeuronsCount = layerSizes[i - 1]
                    Neuron(incomingNeuronsCount)
                }
                layers.add(Layer(neurons))
            }
            return NeuronNetwork(layerSizes[0], layers)
        }
    }

    /**
     * @param inputs The initial inputs which are used to predict an answer.
     * @return Returns the calculated output of the last neurons.
     */
    fun predict(inputs: List<Double>): List<Double> {
        if (this.inputs != inputs.size) {
            val message = "Inputs size doesn't match input neuron amount. Expected ${this.inputs}, got ${inputs.size}"
            throw IllegalArgumentException(message)
        }

        var layerInputs = inputs  // Inputs of the previous layer
        for (layer in layers) {
            layerInputs = layer.calculateOutput(layerInputs)
        }
        return layerInputs // Last inputs are the final outputs
    }

    fun loss(input: List<Double>, expectedOutput: List<Double>): Double {
        val outputs = predict(input)
        val outputLayer = layers.last()
        val loss = outputLayer.neurons.mapIndexed { i, neuron ->
            neuron.cost(outputs[i], expectedOutput[i])
        }.sum()
        return loss
    }

    private fun normalize(
        inputs: List<Double>,
        min: Double,
        max: Double
    ): List<Double> = inputs.map { x ->
        (x - min) / (x - max) * 2 - 1
    }

}