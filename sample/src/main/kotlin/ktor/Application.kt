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

    //デフォルト値を設定
    install(ContentTypeChecker){

        //正しいContent-Typeの時の処理
        onSuccess = {
            println("Content-Type: ${call.request.contentType()}")
            call.respond("Success")
        }

        ////間違っているContent-Typeの時の処理
        onError = {
            println("Allowed: [${it.joinToString(", ")}], Request: ${call.request.contentType()}")
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                "Error"
            )
        }

        //Content-Typeが間違っているときに、処理を続けるかどうか
        continueOnError = false
    }

    routing {

        //許可するContent-Typeを記述
        allowContentType(
            ContentType.Application.Json, ContentType.Application.JavaScript
        ){
            get("hello"){
                //Something
            }

            post("hello"){
                //Something
            }
        }

        //デフォルト値をオーバーライドすることも可能
        allowContentType(
            ContentType.Audio.Any,
            onSuccess = {
                call.respond(HttpStatusCode.IAmATeaPot)
            }
        ){
            get("world"){
                //Something
            }
            post("world") {
                //Something
            }
        }
    }
}

val HttpStatusCode.Companion.IAmATeaPot get() = HttpStatusCode(418, "I'm a tea pot")
