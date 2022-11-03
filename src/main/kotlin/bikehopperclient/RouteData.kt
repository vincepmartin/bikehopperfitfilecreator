package bikehopperclient
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
    val legs: ArrayList<Leg>,
    val points: Points
)

@Serializable
data class Leg(
    val type: String,
    // val geometry: ArrayList<ArrayList<Points>>
    val geometry: Points
)

@Serializable
data class Points(
    val type: String,
    val coordinates: ArrayList<ArrayList<Double>>
)