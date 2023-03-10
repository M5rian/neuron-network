import kotlin.random.Random

/**
 * @property incomingNeurons Amount of neurons from the previous layer.
 * @property weights The weight for each incoming neuron.
 * @property bias Bias value for this neuron.
 */
class Neuron(
    private val incomingNeurons: Int,
    val weights: DoubleArray = DoubleArray(incomingNeurons) { randomWeight() },
    var bias: Double = 0.0
) {
    var costGradientBias: Double = 0.0
    val costGradientWeights = MutableList(weights.size) { 0.0 }

    companion object {
        private fun randomWeight(): Double {
            return Random.nextDouble(-1.0, 1.0)
        }
    }

    fun calculateOutput(inputs: List<Double>): Double {
        // Weighted sum of the inputs and their weights
        // x1*w1 + x2*w2 + ... + xn*wn
        val weightedInput = inputs
            .mapIndexed { index, input -> input * weights[index] }
            .sum()
        // Run the weighted input through an activation function
        return activationFunction(weightedInput + bias)
    }

    private fun activationFunction(weightedInput: Double): Double {
        return ActivationFunction.SIGMOID.evaluate(weightedInput)
    }

    fun loss(output: Double, expectedOutput: Double): Double {
        // Calculate difference between expected value and given value
        val error = output - expectedOutput
        // Bigger differences get even bigger + negative values become positive
        return error * error
    }

    /**
     * Updates the weights and biases based on the cost gradients.
     *
     * @param learnRate
     */
    fun applyGradients(learnRate: Double) {
        bias -= costGradientBias * learnRate
        for (i in weights.indices) {
            weights[i] -= costGradientWeights[i] * learnRate
        }
    }

}