@file:Suppress("NonAsciiCharacters", "TestFunctionName", "ClassName")

package ktor

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    class allowContentTypeのテスト {

        @Test
        fun ホワイトリストに含まれるContentTypeの時はSuccessを返す(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/allow") {
                    addHeader("Content-Type", ContentType.Application.JavaScript.toString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Success", response.content)
                }
            }
        }


        @Test
        fun ホワイトリストに含まれないContentTypeの時はSuccessを返す(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/allow") {
                    addHeader("Content-Type", ContentType.Any.toString())
                }.apply {
                    assertEquals(HttpStatusCode.UnsupportedMediaType, response.status())
                    assertEquals("Error", response.content)
                }
            }
        }
    }


    class negativeContentTypeのテスト {

        @Test
        fun ブラックリストに含まれないContentTypeの時はSuccessを返す(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/negative") {
                    addHeader("Content-Type", ContentType.Application.JavaScript.toString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Success", response.content)
                }
            }
        }


        @Test
        fun ブラックリストに含まれるContentTypeの時はSuccessを返す(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/negative") {
                    addHeader("Content-Type", ContentType.Application.Xml.toString())
                }.apply {
                    assertEquals(HttpStatusCode.UnsupportedMediaType, response.status())
                    assertEquals("Error", response.content)
                }
            }
        }
    }


    class 共通のテスト {

        @Test
        fun continueOnErrorがfalseの時に違うContentTypeが来ると処理が中断される(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/continue-on-error") {
                    addHeader("Content-Type", ContentType.Audio.Any.toString())
                }.apply {
                    assertEquals(HttpStatusCode.UnsupportedMediaType, response.status())
                }
            }
        }

        @Test
        fun それぞれのパラメーターはオーバーライドできる(){

            withTestApplication({ module() }) {

                handleRequest(HttpMethod.Get, "/override") {
                    addHeader("Content-Type", ContentType.Audio.Any.toString())
                }.apply {
                    assertEquals(HttpStatusCode.IAmATeaPot, response.status())
                }
            }
        }
    }

    @Test
    fun test(){}
}
