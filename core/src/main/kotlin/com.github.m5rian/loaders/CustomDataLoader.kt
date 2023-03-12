package loaders

class CustomDataLoader(val set:String) {

    data class Data(val x: Double, val y: Double, val classification: Byte)

    fun loadSamples(): List<Data> {
        val text = this::class.java.getResource("/$set/data.sample")?.readText() ?: error("File not found")
        return text.split("\n".toRegex()).mapNotNull { line ->
            if (line.startsWith("#")) return@mapNotNull null
            val data = line.split("|")
            Data(
                x = data[0].toDoubleOrNull() ?: 0.0,
                y = data[1].toDoubleOrNull() ?: 0.0,
                classification = data[2].toByteOrNull() ?: 0
            )
        }
    }

}