package bikehopperclient

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
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
    private val url = "https://api-staging.bikehopper.org"
    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(Logging)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @Throws(ClientRequestException::class)
    fun fetchRoute(params: Map<String, List<String>>): RouteData {
        val routeData: RouteData
        runBlocking {
            val response: HttpResponse = client.get(url) {
                url {
                    appendPathSegments(listOf("v1", "graphhopper", "route-pt"))
                    params.forEach { (key, values) -> values.forEach { value -> parameters.append(key, value) } }
                }
            }
            routeData = response.body()
        }
        return routeData
    }
}
