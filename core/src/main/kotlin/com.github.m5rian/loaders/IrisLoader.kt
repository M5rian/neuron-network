package loaders

enum class PlantClassification {
    SETOSA,
    VERSICOLOR,
    VIRGINICA;

    companion object {
        fun fromName(name: String): PlantClassification = when (name) {
            "Iris-setosa"     -> SETOSA
            "Iris-versicolor" -> VERSICOLOR
            "Iris-virginica"  -> VIRGINICA
            else              -> throw IllegalStateException()
        }
    }
}

data class Plant(
    val classification: PlantClassification,
    val sepalLength: Double,
    val sepalWidth: Double,
    val petalLength: Double,
    val petalWidth: Double
)

object IrisLoader {

    fun loadPlantSet(filename: String): List<Plant> {
        val text = this::class.java.getResource(filename)?.readText() ?: error("File not found")
        return text.split("\n".toRegex()).mapNotNull {
            val data = it.split(",")
            if (data.size != 5) return@mapNotNull null
            Plant(
                PlantClassification.fromName(data[4]),
                data[0].toDoubleOrNull() ?: 0.0,
                data[1].toDoubleOrNull() ?: 0.0,
                data[2].toDoubleOrNull() ?: 0.0,
                data[3].toDoubleOrNull() ?: 0.0
            )
        }
    }

}