import bikehopperclient.BikeHopperClient
import io.javalin.Javalin

fun main() {
    println("Starting up Bike Hopper Fit File Creator Version 1.0")
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            // TODO: Convert all println statements to use the logger instead.
            println("request...")
            println(ctx.queryParamMap())
            val bhClient = BikeHopperClient()
            bhClient.fetchRoute(params = ctx.queryParamMap())
        }
        .start(9001)
}