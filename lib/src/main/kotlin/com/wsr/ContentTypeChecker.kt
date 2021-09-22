package com.wsr

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*


typealias contentTypeCheckerCallback = suspend PipelineContext<Unit, ApplicationCall>.(List<ContentType>) -> Unit


class ContentTypeChecker(private val configuration: Configuration) {

    //設定ファイル
    class Configuration{

        //正しいContent-Typeの時
        var onSuccess: contentTypeCheckerCallback = {}

        //間違っている時
        var onError: contentTypeCheckerCallback = {}

        //間違っているときに、処理を続けるかどうか
        var continueOnError: Boolean = true
    }

    //差し込む処理
    fun intercept(
        contentTypeRoute: Route,
        allowContentTypes: List<ContentType>,
        onSuccess: contentTypeCheckerCallback? = null,
        onError: contentTypeCheckerCallback? = null,
        continueOnError: Boolean? = null
    ){

        //Featuresに処理を差し込む
        contentTypeRoute.intercept(ApplicationCallPipeline.Features){

            //送られてきたContent-Type
            val contentType = call.request.contentType()

            //許可された中に含まれていれば
            if(allowContentTypes.contains(contentType)){

                //独自の処理があればそれを実行
                if(onSuccess != null){
                    onSuccess(this, allowContentTypes)
                }
                //なければ、install時の処理を実行
                else{
                    configuration.onSuccess(this, allowContentTypes)
                }

            }
            //許可された中に含まれていなければ
            else{

                //独自の処理があればそれを実行
                if(onError != null){
                    onError(this, allowContentTypes)
                }
                //なければ、install時の処理を実行
                else{
                    configuration.onError(this, allowContentTypes)
                }

                //処理を続けない場合（独自のもので判定、なければinstall時のもので判定）
                if(
                    (continueOnError != null && !continueOnError) ||
                    (continueOnError == null && !configuration.continueOnError)
                ) {
                    finish()
                }
            }
        }
    }

    companion object Feature: ApplicationFeature<ApplicationCallPipeline, Configuration, ContentTypeChecker>{

        override val key: AttributeKey<ContentTypeChecker> = AttributeKey("ContentTypeChecker")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): ContentTypeChecker = ContentTypeChecker(Configuration().apply(configure))
    }
}



private class ContentTypeCheckerRouteSelector : RouteSelector(){
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }
}



@Suppress("unused")
fun Route.allowContentType(
    vararg allowContentTypes: ContentType,
    onSuccess: contentTypeCheckerCallback? = null,
    onError: contentTypeCheckerCallback? = null,
    continueOnError: Boolean? = null,
    build: Route.() -> Unit
): Route{

    //ルートの作成
    val contentTypeRoute = createChild(ContentTypeCheckerRouteSelector())

    //featureのintercept
    application.feature(ContentTypeChecker)
        .intercept(
            contentTypeRoute,
            allowContentTypes.toList(),
            onSuccess,
            onError,
            continueOnError
        )

    //下位のルートの作成
    contentTypeRoute.build()

    return contentTypeRoute
}
