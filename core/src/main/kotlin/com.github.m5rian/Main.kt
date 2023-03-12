import java.awt.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteReader(private val bytes: ByteArray) {
    private val buffer = ByteBuffer.wrap(bytes)

    init {
        // Use most significant byte (msb), which is the same as big endian
        buffer.order(ByteOrder.BIG_ENDIAN)
    }

    fun readInt(): Int = buffer.int
    fun readUByte(): UByte = buffer.get().toUByte()
}

data class ImageSet(
    val count: Int,
    val width: Int,
    val height: Int,
    val images: List<Image>
) {
    val pixels = width * height
}

data class Image(
    val rows: Array<UByteArray>,
    val pixels: UByteArray
)

suspend fun main() {
    /*
    val trainingImages = loaders.Idx3UbyteLoader.loadImageSet("train-images-idx3-ubyte")
    println("Image size = ${trainingImages.width}x${trainingImages.height} (${trainingImages.count}x)")
    val trainingLabels = loaders.Idx3UbyteLoader.loadLabelSet("train-labels.idx1-ubyte")
    println("Labels = ")
    for (i in 0 until 10) {
        println(trainingLabels[i])
    }
    val neuronNetwork = NeuronNetwork.fromLayerSizes(trainingImages.pixels, 2, 10)
    */

    /*
    val neuronNetwork = NeuronNetwork.fromLayerSizes(2, 2, 2)
    val visualizer = visualizer.Visualizer2d(neuronNetwork, -100..1200, -100..500)
    visualizer.render()
    */


}

fun getColour(outputs: List<Double>): Color {
    return if (outputs.indexOf(outputs.max()) == 0) Color.GREEN
    else if (outputs.indexOf(outputs.max()) == 1) Color.RED
    else {
        println("hä? inputs = $outputs")
        Color.GRAY
    }
}