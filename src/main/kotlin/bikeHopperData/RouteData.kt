package bikeHopperData
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class RouteData(val paths: List<Paths>)

@Serializable
data class Paths(
    val distance: Double,
    val weight: Double,
    val time: Int,
    val transfers: Int,
    val points_encoded: Boolean,
    val bbox: ArrayList<Double>,
    val points: Points
)

@Serializable
data class Points(val type: String, val coordinates: ArrayList<ArrayList<Double>>)