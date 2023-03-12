import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * @property inputs
 * @property layers All layers except the first one.
 */
@Serializable
class NeuronNetwork(
    val inputs: Int,
    val layers: List<Layer>
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

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

        fun fromFile(filePath: String): NeuronNetwork? {
            return try {
                val text = File(filePath).inputStream().bufferedReader().use { it.readText() }
                val neuronNetwork = json.decodeFromString<NeuronNetwork>(text)
                neuronNetwork
            } catch (e: NullPointerException) {
                null
            }
        }
    }

    fun toFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) file.createNewFile()
        val text = json.encodeToString(this)
        file.writeText(text)
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

    fun normalizeSet(data: List<TrainingData>, max: List<Double>): List<TrainingData> {
        return data.map {
            val inputs = it.inputs.mapIndexed { index, num -> num / max[index] }
            val outputs = it.outputs
            TrainingData(inputs, outputs)
        }
    }

    fun normalize(input: List<Double>, max: List<Double>): List<Double> {
        return input.mapIndexed { index, num -> num / max[index] }
    }

    private fun cost(input: List<Double>, expectedOutput: List<Double>): Double {
        val outputs = predict(input)
        val outputLayer = layers.last()
        val loss = outputLayer.neurons.mapIndexed { i, neuron ->
            neuron.loss(outputs[i], expectedOutput[i])
        }.sum()
        return loss
    }

    fun averageCost(data: List<TrainingData>): Double {
        val totalCost = data.sumOf { cost(it.inputs, it.outputs) }
        return totalCost / data.size
    }

    fun test(trainingData: List<TrainingData>): Int {
        return trainingData.count {
            val predictedOutputs = predict(it.inputs)
            val predictedOutput = predictedOutputs.indexOf(predictedOutputs.max())
            val output = it.outputs.indexOf(it.outputs.max())
            /*
            println("koordinaten von dem punkt = ")
            println("inputs = ${it.inputs}")
            println("Ich suche = ${getColour(it.outputs)}")
            println("Ich habe = ${getColour(predictedOutputs)}")
            */
            predictedOutput == output
        }
    }

    fun learn(trainingData: List<TrainingData>, learnRate: Double) {
        val distance = 0.00001
        val originalCost = averageCost(trainingData)

        layers.forEach { layer ->
            layer.neurons.forEach { neuron ->

                val weights = neuron.weights
                for (i in weights.indices) {
                    weights[i] += distance

                    /* Next we calculate the slope of the cost function.
                      This can be done by taking the 2 cost points and using them as y values.
                      (y2 - y1) / (x2 - x1)
                      We know the distance so the formula can be simplified to:
                      (y2 - y1) / distance
                    */
                    val newCost = averageCost(trainingData)
                    val slope = (newCost - originalCost) / distance
                    neuron.costGradientWeights[i] = slope

                    weights[i] -= distance // Reset weight to not throw of next calculations
                }

                neuron.bias += distance
                val newCost = averageCost(trainingData)
                val slope = (newCost - originalCost) / distance
                neuron.costGradientBias = slope
                neuron.bias -= distance
            }
        }

        layers
            .flatMap { it.neurons.toList() }
            .forEach { it.applyGradients(learnRate) }
    }

}