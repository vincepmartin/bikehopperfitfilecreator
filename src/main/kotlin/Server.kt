import io.javalin.Javalin
import com.garmin.fit.Fit

fun main() {
    println("Starting up Bike Hopper Fit File Creator Version 1.0")
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            println("request...")
            println(ctx.queryParamMap())
            println("main(): Creating the client object...")
            val bhClient = BikeHopperClient()
            println("main(): Trying to fetch a route...")
            bhClient.fetchRoute(params = ctx.queryParamMap())
        }
        .start(9001)
}