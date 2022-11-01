package bikeHopperData

data class RouteData(val paths: Paths)

data class Paths(val distance: Double, val weight: Double, val time: Int, val transfers: Int, val pointsEncoded: Boolean, val bbox: ArrayList<Double>, val points: Points)

data class Points(val type: String, val coordinates: ArrayList<ArrayList<Double>>)