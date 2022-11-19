import bikehopperclient.BikeHopperClient
import bikehopperfilecreator.BikeHopperFileCreator
import bikehopperfilecreator.BikeHopperFileCreatorException
import io.javalin.Javalin
import io.ktor.client.plugins.*

fun main() {
    val app = Javalin.create(/* TODO: Config */)

    app.get("/fit") { ctx ->
        val bhClient = BikeHopperClient()
        val routeData = bhClient.fetchRoute(ctx.queryParamMap())
        val bikeHopperFileCreator = BikeHopperFileCreator(routeData)
        ctx.contentType("application/vnd.ant.fit")
        ctx.result(bikeHopperFileCreator.getBuffer())
    }

    // Handle exceptions thrown from the BikeHopperClient's fetchRoute method
    app.exception(ClientRequestException::class.java) { e, ctx ->
        ctx.status(400)
        ctx.result(e.localizedMessage)
    }

    // Handle exceptions thrown when creating the FIT file
    app.exception(BikeHopperFileCreatorException::class.java) { e, ctx ->
        ctx.status(500)
        ctx.result(e.localizedMessage)
    }
    app.start(9001)
}