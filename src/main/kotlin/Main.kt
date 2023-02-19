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
    val trainingImages = Idx3UbyteLoader.loadImageSet("train-images-idx3-ubyte")
    println("Image size = ${trainingImages.width}x${trainingImages.height} (${trainingImages.count}x)")
    val trainingLabels = Idx3UbyteLoader.loadLabelSet("train-labels.idx1-ubyte")
    println("Labels = ")
    for (i in 0 until 10) {
        println(trainingLabels[i])
    }
    val neuronNetwork = NeuronNetwork.fromLayerSizes(trainingImages.pixels, 2, 10)
    */

    val neuronNetwork = NeuronNetwork.fromLayerSizes(2, 2, 2)
    val visualizer = visualizer.Visualizer2d(neuronNetwork, -50..1200, -50..500)
    visualizer.render()


    /*
    val trainingSamples = IrisLoader
        .loadPlantSet("datasets/iris/iris.data")
        .filter { it.classification != PlantClassification.SETOSA }

    val neuronNetwork = NeuronNetwork.fromLayerSizes(2, 2, 2)
    val visualizer = Visualizer2d(neuronNetwork, 0 until 15, 0 until 25, 0.02)
    visualizer.dataPoints(trainingSamples) {
        val classification = if (it.classification == PlantClassification.VIRGINICA) 1 else -1
        DataPoint(classification, it.petalLength, it.sepalLength)
    }
    visualizer.render()
     */
}

