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
    val points: Points,
    val instructions: ArrayList<Instructions>
)

@Serializable
data class Leg(
    val type: String,
    val geometry: Points
)

@Serializable
data class Points(
    val type: String,
    val coordinates: ArrayList<ArrayList<Double>>
)

@Serializable
data class Instructions(
    val text: String,
    val street_name: String,
    val sign: Int,
    val interval: ArrayList<Int>
)