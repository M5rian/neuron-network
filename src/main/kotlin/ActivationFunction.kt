import kotlin.math.exp
import kotlin.math.max
import kotlin.math.tanh

enum class ActivationFunction(private val function: (Double) -> Double) {
    LINEAR({ x -> x }),
    HEAVISIDE({ x -> if (x > 0) 1.0 else 0.0 }),
    SIGMOID({ x -> 1.0f / (1.0f + exp(-x)) }),
    RELU({ x -> max(0.0, x) }),
    TANH({ x -> tanh(x) });

    fun evaluate(weightedInput: Double): Double = function.invoke(weightedInput)
}