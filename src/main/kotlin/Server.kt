import io.javalin.Javalin

fun main() {
    println("Starting up Bike Hopper Fit File Creator Version 1.0")
    val app = Javalin.create(/*config*/)
        .get("/") { ctx ->
            ctx.result("Bike Hopper Fit File Creator 1.0 GET Request Served")
        }
        .start(9001)
}