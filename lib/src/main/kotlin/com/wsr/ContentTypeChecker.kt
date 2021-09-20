package com.wsr

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*


class ContentTypeChecker(configuration: Configuration) {

    class Configuration{

    }

    companion object Feature: ApplicationFeature<ApplicationCallPipeline, Configuration, ContentTypeChecker>{

        override val key: AttributeKey<ContentTypeChecker> = AttributeKey("ContentTypeChecker")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): ContentTypeChecker {

            return ContentTypeChecker(Configuration().apply(configure))
        }
    }
}

class ContentTypeCheckerRouteSelector : RouteSelector(){
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

}

fun Route.contentTypeChecker(
    contentTypes: List<ContentType>,
    callback: Route.() -> Unit
): Route{
    val route = createChild(ContentTypeCheckerRouteSelector())

    route.intercept(ApplicationCallPipeline.Features){

        println("Content Type: ${call.request.contentType()}")

        if(contentTypes.contains(call.request.contentType())){
            proceed()
        }else{
            throw Exception("CONTENT TYPE ERROR")
        }
    }

    callback(route)

    return route
}
