package ktor

import com.wsr.ContentTypeChecker
import com.wsr.contentTypeChecker
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module(){

    install(ContentTypeChecker){}

    routing {
        contentTypeChecker(listOf(ContentType.Application.Json)){
            get{
                call.respondText("Hello World")
            }
        }
    }
}
