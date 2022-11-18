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

public class BikeHopperClient() {
    // TODO: Have the url chosen via some env var or flag or something
    // private val url = "https://api-bikehopper-staging.techlabor.org"
    private val url = "https://api-staging.bikehopper.org"
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
    /*
    curl 'https://api.bikehopper.org/v1/graphhopper/route-pt?locale=en-US&elevation=true&useMiles=false&layer=OpenStreetMap&profile=pt&optimize=true&pointsEncoded=false&pt.earliest_departure_time=2022-11-04T23%3A01%3A56.126Z&pt.connecting_profile=bike2&pt.arrive_by=false&details=cycleway&details=road_class&details=street_name
    &point=37.78306%2C-122.45867
    &point=37.78516%2C-122.46238' \
    */
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
                    parameters.append("point","37.78516,-122.46238")
                }
            }
            // TODO: Make sure you have all the data that you need.
            routeData = response.body()
        }
        return routeData
    }
}
