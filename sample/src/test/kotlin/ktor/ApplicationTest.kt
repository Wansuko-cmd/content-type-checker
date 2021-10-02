@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package ktor

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun 正しいContentTypeの時はSuccessを返す(){

        withTestApplication({ module() }) {

            handleRequest(HttpMethod.Get, "/hello") {
                addHeader("Content-Type", ContentType.Application.JavaScript.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Success", response.content)
            }
        }
    }


    @Test
    fun 間違ったContentTypeの時はSuccessを返す(){

        withTestApplication({ module() }) {

            handleRequest(HttpMethod.Get, "/hello") {
                addHeader("Content-Type", ContentType.Any.toString())
            }.apply {
                assertEquals(HttpStatusCode.UnsupportedMediaType, response.status())
                assertEquals("Error", response.content)
            }
        }
    }

    @Test
    fun オーバーライドを行うことも可能(){

        withTestApplication({ module() }) {

            handleRequest(HttpMethod.Get, "/world") {
                addHeader("Content-Type", ContentType.Audio.Any.toString())
            }.apply {
                assertEquals(HttpStatusCode.IAmATeaPot, response.status())
            }
        }
    }
}
