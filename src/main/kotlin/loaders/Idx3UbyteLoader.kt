package loaders

import ByteReader
import Image
import ImageSet
import me.tongfei.progressbar.ProgressBar

object Idx3UbyteLoader {

    private fun readFile(filename: String): ByteReader {
        val bytes = this::class.java.getResourceAsStream(filename)?.readAllBytes() ?: error("Image file not found")
        return ByteReader(bytes)
    }

    fun loadImageSet(filename: String): ImageSet {
        val reader = readFile(filename)

        val magicNumber = reader.readInt()
        val imageCount = reader.readInt()
        val rows = reader.readInt()
        val columns = reader.readInt()

        val images = mutableListOf<Image>()
        val progress = ProgressBar("Loading training images", imageCount.toLong())
        for (imageIndex in 0 until imageCount) {
            progress.step()
            progress.refresh()

            val pixels = Array(rows) { UByteArray(columns) }
            val allPixels = UByteArray(rows * columns) { 0.toUByte() }
            // Pixels are organized row-wise.
            for (row in 0 until rows) {
                for (column in 0 until columns) {
                    val ubyte = reader.readUByte()
                    pixels[row][column] = ubyte
                    allPixels[row * 1 + column]
                }
            }
            val image = Image(pixels, allPixels)
            images.add(image)
        }
        progress.close()

        return ImageSet(imageCount, rows, columns, images)
    }

    fun loadLabelSet(filename: String): UByteArray {
        val reader = readFile(filename)

        val magicNumber = reader.readInt()
        val labelCount = reader.readInt()
        val labels = UByteArray(labelCount)
        for (i in 0 until labelCount) {
            labels[i] = reader.readUByte()
        }
        return labels
    }

}