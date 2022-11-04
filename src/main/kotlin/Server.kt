import bikehopperclient.BikeHopperClient
import bikehopperfilecreator.BikeHopperFileCreator
import io.javalin.Javalin

fun main() {
    println("Starting up Bike Hopper Fit File Creator Version 1.0")
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            val bhClient = BikeHopperClient()
            val routeData = bhClient.fetchRoute(params = ctx.queryParamMap())
            val bikeHopperFileCreator = BikeHopperFileCreator(routeData)
            ctx.contentType("application/vnd.ant.fit")
            // TODO: WUT?  Figure out a better way to handle this.
            bikeHopperFileCreator.getBuffer()?.let { ctx.result(it) }
        }
        .start(9001)
}