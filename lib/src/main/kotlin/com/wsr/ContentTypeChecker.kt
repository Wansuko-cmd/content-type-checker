package com.wsr

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*


class ContentTypeChecker(private val configuration: Configuration) {

    //設定ファイル
    class Configuration{

        //正しいContent-Typeの時
        var onSuccess: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit = {}

        //間違っている時
        var onError: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit = {}

        //間違っているときに、処理を続けるかどうか
        var continueOnError: Boolean = true
    }

    //差し込む処理
    fun intercept(
        contentTypeRoute: Route,
        allowContentTypes: List<ContentType>,
        onSuccess: (suspend PipelineContext<Unit, ApplicationCall>.() -> Unit)? = null,
        onError: (suspend PipelineContext<Unit, ApplicationCall>.() -> Unit)? = null,
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
                    onSuccess(this)
                }
                //なければ、install時の処理を実行
                else{
                    configuration.onSuccess(this)
                }

                proceed()

            }
            //許可された中に含まれていなければ
            else{

                //独自の処理があればそれを実行
                if(onError != null){
                    onError(this)
                }
                //なければ、install時の処理を実行
                else{
                    configuration.onError(this)
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



class ContentTypeCheckerRouteSelector : RouteSelector(){
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }
}



fun Route.allowContentType(
    allowContentTypes: List<ContentType>,
    onSuccess: (suspend PipelineContext<Unit, ApplicationCall>.() -> Unit)? = null,
    onError: (suspend PipelineContext<Unit, ApplicationCall>.() -> Unit)? = null,
    continueOnError: Boolean? = null,
    build: Route.() -> Unit
): Route{

    val contentTypeRoute = createChild(ContentTypeCheckerRouteSelector())

    application.feature(ContentTypeChecker)
        .intercept(
            contentTypeRoute,
            allowContentTypes,
            onSuccess,
            onError,
            continueOnError
        )

    contentTypeRoute.build()

    return contentTypeRoute
}
