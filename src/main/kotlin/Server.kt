import bikehopperclient.BikeHopperClient
import bikehopperfilecreator.BikeHopperFileCreator
import bikehopperfilecreator.BikeHopperFileCreatorException
import io.javalin.Javalin
import io.ktor.client.plugins.*

fun main() {
    val app = Javalin.create(/* TODO: Config */)

    app.get("/fit") { ctx ->
        val bhClient = BikeHopperClient()

        // Use bhClient to fetch our routes after pulling out the "path" param which is only used locally.
        val routeData = bhClient.fetchRoute(ctx.queryParamMap().filter{ it.key != "path"})
        // Quick check that the path we choose actually exists and has just a single leg.
        // TODO: Figure out a better check eventually.
        val path = ctx.queryParamAsClass<Int>("path", Int::class.java).check({it >= 0 && it < routeData.paths.size && routeData.paths[it].legs.size == 1 }, "Invalid path requested").get()
        val bikeHopperFileCreator = BikeHopperFileCreator(routeData, path)
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