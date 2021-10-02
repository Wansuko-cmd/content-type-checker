package ktor

import com.wsr.ContentTypeChecker
import com.wsr.allowContentType
import com.wsr.negativeContentType
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

        //allowContentTypeにおいて正しいContent-Typeの時の処理
        onSuccess = {
            println("Content-Type: ${call.request.contentType()}")
            call.respond("Success")
        }

        //allowContentTypeにおいて間違っているContent-Typeの時の処理
        onErrorWhenAllow = {
            println("Allowed: [${it.joinToString(", ")}], Request: ${call.request.contentType()}")
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                "Error"
            )
        }

        onErrorWhenNegative = {
            println("Negative: [${it.joinToString(", ")}], Request: ${call.request.contentType()}")
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
            get("allow") {
                //Something
            }

            post("allow") {
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
            get("override") {
                //Something
            }
            post("override") {
                //Something
            }
        }


        //許可しないContent-Typeを記述
        negativeContentType(
            ContentType.Application.Xml, ContentType.Application.GZip
        ) {
            get("negative") {
                //Something
            }

            post("negative") {
                //Something
            }
        }
    }
}

val HttpStatusCode.Companion.IAmATeaPot get() = HttpStatusCode(418, "I'm a tea pot")
