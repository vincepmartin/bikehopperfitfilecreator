import bikehopperclient.BikeHopperClient
import bikehopperfilecreator.BikeHopperFileCreator
import io.javalin.Javalin
/*
curl 'https://api.bikehopper.org/v1/graphhopper/route-pt?locale=en-US&elevation=true&useMiles=false&layer=OpenStreetMap&profile=pt&optimize=true&pointsEncoded=false&pt.earliest_departure_time=2022-11-04T23%3A01%3A56.126Z&pt.connecting_profile=bike2&pt.arrive_by=false&details=cycleway&details=road_class&details=street_name&point=37.78306%2C-122.45867&point=37.78516%2C-122.46238' \
*/
fun main() {
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            val bhClient = BikeHopperClient()
            val routeData = bhClient.fetchRoute(params = ctx.queryParamMap())
            val bikeHopperFileCreator = BikeHopperFileCreator(routeData)
            ctx.contentType("application/vnd.ant.fit")
            ctx.result(bikeHopperFileCreator.getBuffer())
        }
        .start(9001)
}