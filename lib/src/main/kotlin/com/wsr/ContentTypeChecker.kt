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

        //allowContentTypeにおいての処理
        var onSuccessWhenAllow: contentTypeCheckerCallback? = null

        var onErrorWhenAllow: contentTypeCheckerCallback? = null

        //negativeContentTypeにおいての処理
        var onSuccessWhenNegative: contentTypeCheckerCallback? = null

        var onErrorWhenNegative: contentTypeCheckerCallback? = null

        //間違っているときに、処理を続けるかどうか
        var continueOnError: Boolean = true
    }


    //成功時とエラー時のハンドリングを行う関数
    private suspend fun PipelineContext<Unit, ApplicationCall>.executeHandling(
        isSuccess: Boolean,
        onSuccess: suspend () -> Unit,
        onError: suspend () -> Unit,
        continueOnError: Boolean
    ){

        //許可された中に含まれていれば成功処理
        if (isSuccess) onSuccess()

        //許可された中に含まれていなければ
        else {

            //エラー処理
            onError()

            //処理を続けない場合パイプラインを終了する
            if (continueOnError) finish()
        }
    }


    //差し込む処理
    internal fun interceptOnAllow(
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

            executeHandling(

                //送られてきたContent-Typeがホワイトリストに含まれていれば成功
                isSuccess = allowContentTypes.contains(contentType),

                //独自のものを優先し、なければデフォルト値のものを実行
                onSuccess = {
                    if (onSuccess != null) {
                        onSuccess(this, allowContentTypes)
                    }
                    else {
                        if(configuration.onSuccessWhenAllow != null) configuration.onSuccessWhenAllow!!(this, allowContentTypes)
                        else configuration.onSuccess(this, allowContentTypes)
                    }
                },
                onError =  {
                    if (onError != null) {
                        onError(this, allowContentTypes)
                    }
                    else {
                        if(configuration.onErrorWhenAllow != null) configuration.onErrorWhenAllow!!(this, allowContentTypes)
                        else configuration.onError(this, allowContentTypes)
                    }
                },
                continueOnError = continueOnError ?: configuration.continueOnError
            )
        }
    }


    //差し込む処理
    internal fun interceptOnNegative(
        contentTypeRoute: Route,
        negativeContentTypes: List<ContentType>,
        onSuccess: contentTypeCheckerCallback? = null,
        onError: contentTypeCheckerCallback? = null,
        continueOnError: Boolean? = null
    ) {

        //Featuresに処理を差し込む
        contentTypeRoute.intercept(ApplicationCallPipeline.Features) {

            //送られてきたContent-Type
            val contentType = call.request.contentType()

            executeHandling(

                //送られてきたContent-Typeがブラックリストに含まれていなければ成功
                isSuccess = !negativeContentTypes.contains(contentType),

                //独自のものを優先し、なければデフォルト値のものを実行
                onSuccess = {
                    if (onSuccess != null) {
                        onSuccess(this, negativeContentTypes)
                    }
                    else {
                        if(configuration.onSuccessWhenAllow != null) configuration.onSuccessWhenNegative!!(this, negativeContentTypes)
                        else configuration.onSuccess(this, negativeContentTypes)
                    }
                },
                onError =  {
                    if (onError != null) {
                        onError(this, negativeContentTypes)
                    }
                    else {
                        if(configuration.onErrorWhenAllow != null) configuration.onErrorWhenNegative!!(this, negativeContentTypes)
                        else configuration.onError(this, negativeContentTypes)
                    }
                },
                continueOnError = continueOnError ?: configuration.continueOnError
            )
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
        .interceptOnAllow(
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


@Suppress("unused")
fun Route.negativeContentType(
    vararg negativeContentTypes: ContentType,
    onSuccess: contentTypeCheckerCallback? = null,
    onError: contentTypeCheckerCallback? = null,
    continueOnError: Boolean? = null,
    build: Route.() -> Unit
): Route{

    //ルートの作成
    val contentTypeRoute = createChild(ContentTypeCheckerRouteSelector())

    //featureのintercept
    application.feature(ContentTypeChecker)
        .interceptOnNegative(
            contentTypeRoute,
            negativeContentTypes.toList(),
            onSuccess,
            onError,
            continueOnError
        )

    //下位のルートの作成
    contentTypeRoute.build()

    return contentTypeRoute
}
