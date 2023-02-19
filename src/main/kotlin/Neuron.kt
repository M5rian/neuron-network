/**
 * @property incomingNeurons Amount of neurons from the previous layer.
 * @property weights The weight for each incoming neuron.
 * @property bias Bias value for this neuron.
 */
class Neuron(
    private val incomingNeurons: Int,
    val weights: DoubleArray = DoubleArray(incomingNeurons) { 1.0 },
    var bias: Double = 0.0
) {

    fun calculateOutput(inputs: List<Double>): Double {
        // Weighted sum of the inputs and their weights
        // x1*w1 + x2*w2 + ... + xn*wn
        val weightedInput = inputs
            .mapIndexed { index, input -> input * weights[index] }
            .reduce { current, new -> current + new }
        // Run the weighted input through an activation function
        return activationFunction(weightedInput + bias)
    }

    private fun activationFunction(weightedInput: Double): Double {
        return ActivationFunction.SIGMOID.evaluate(weightedInput)
    }

    fun cost(output: Double, expectedOutput: Double): Double {
        // Calculate difference between expected value and given value
        val error = output - expectedOutput
        // Bigger differences get even bigger + negative values become positive
        return error * error
    }

}