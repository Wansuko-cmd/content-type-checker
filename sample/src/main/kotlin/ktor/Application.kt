package ktor

import Test
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module(){

    routing {
        get{
            call.respondText(Test())
        }
    }
}
