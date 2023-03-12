class Layer(
    val neurons: Array<Neuron>
) {

    /**
     * @param inputs The output of the previous layer (or the initial input).
     * @return Returns the output of each neuron of this layer.
     */
    fun calculateOutput(inputs: List<Double>): List<Double> {
        return neurons.map { it.calculateOutput(inputs) }
    }
}