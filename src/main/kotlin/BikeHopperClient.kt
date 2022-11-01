import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

public class BikeHopperClient() {
    private val url = "https://api-bikehopper-staging.techlabor.org"
    private val client = HttpClient(CIO) {
        install(Logging)
    }

    // TODO: Eventually make this grab params from the front end.  For now just use this manual version.
    fun fetchRoute(params: Map<String, List<String>>) {
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
            println("About to access $url")
            println(response.bodyAsText())
        }
    }
}
