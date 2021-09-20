package ktor

import com.wsr.ContentTypeChecker
import com.wsr.allowContentType
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module(){

    install(ContentTypeChecker){
        onError = {
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                "${call.request.contentType()}には対応していません"
            )
        }
        continueOnError = true
    }

    routing {
        allowContentType(listOf(ContentType.Application.Json)){
            get{
                call.respondText("Hello World")
            }
        }
    }
}
