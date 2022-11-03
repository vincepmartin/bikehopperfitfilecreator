package bikehopperclient

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

import bikehopperclient.RouteData

public class BikeHopperClient() {
    // TODO: Have the url chosen via some env var or flag or something
    private val url = "https://api-bikehopper-staging.techlabor.org"
    private val client = HttpClient(CIO) {
        install(Logging)
        install(ContentNegotiation) {
             json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
        }
    }

    // TODO: Eventually make this grab params from the front end that are passed to Javalin.  For now just use this manual version.
    fun fetchRoute(params: Map<String, List<String>>): RouteData {
        val routeData: RouteData
        runBlocking {
            val response: HttpResponse = client.get(url) {
                url {
                    appendPathSegments(listOf("v1", "graphhopper", "route-pt"))
                    parameters.append("locale", "en-US")
                    parameters.append("elevation", "true")
                    parameters.append("useMiles", "false")
                    parameters.append("layer", "OpenStreetMap")
                    parameters.append("profile", "pt")
                    parameters.append("optimize", "true")
                    parameters.append("pointsEncoded", "false")
                    parameters.append("pt.earliest_departure_time", "2022-11-01T02:31:44.439Z")
                    parameters.append("pt.connecting_profile", "bike2")
                    parameters.append("pt.arrive_by", "false")
                    parameters.append("details", "cycleway")
                    parameters.append("details","road_class")
                    parameters.append("details","street_name")
                    parameters.append("point","37.78306,-122.45867")
                    parameters.append("point","37.79183,-122.39415")
                }
            }
            // TODO: Make sure you have all the data that you need.
            routeData = response.body()
        }

        printRouteData(routeData)
        return routeData
    }

    fun printRouteData(routeData: RouteData) {
        println("*** Printing route points ***")
        for (p in routeData.paths[0].legs[0].geometry.coordinates) {
            println("${p[0]} ${p[1]} ${p[2]}")
        }
        println("*****************************")
    }
}
