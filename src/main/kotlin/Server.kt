import bikehopperclient.BikeHopperClient
import bikehopperfilecreator.BikeHopperFileCreator
import io.javalin.Javalin

fun main() {
    println("Starting up Bike Hopper Fit File Creator Version 1.0")
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            // TODO: Convert all println statements to use the logger instead.
            println("request...")
            println(ctx.queryParamMap())
            val bhClient = BikeHopperClient()
            val routeData = bhClient.fetchRoute(params = ctx.queryParamMap())
            val bikeHopperFileCreator = BikeHopperFileCreator("nachos.fit", routeData)
            ctx.contentType("application/vnd.ant.fit")
            ctx.result(bikeHopperFileCreator.getFile().inputStream())
        }
        .start(9001)
}