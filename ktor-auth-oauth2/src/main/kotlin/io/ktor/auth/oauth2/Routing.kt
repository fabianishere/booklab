/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ktor.auth.oauth2

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.auth.oauth2.util.state
import io.ktor.auth.oauth2.util.toJson
import io.ktor.content.OutgoingContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.ContextDsl
import io.ktor.pipeline.PipelinePhase
import io.ktor.request.contentType
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.post

/**
 * The [PipelinePhase] in which we authorize a client.
 */
internal val Authorization = PipelinePhase("Authorization")

/**
 * Restrict the given route to the given [scopes].
 * In case no valid [AccessToken] principal can be found in the [ApplicationCall], the execution of the route will just
 * continue.
 *
 * @param scopes The scopes to restrict the route to.
 * @param build The block to build the child route.
 */
@ContextDsl
fun Route.scoped(vararg scopes: String = emptyArray(), build: Route.() -> Unit): Route {
    val selector = ScopedRouteSelector(scopes.toHashSet())
    val route = createChild(selector).apply(build)
    route.insertPhaseBefore(ApplicationCallPipeline.Call, Authorization)
    route.intercept(Authorization) {
        val token = context.authentication.principal as? AccessToken<*, *>

        // Verify whether the client has a sufficient scope
        if (token == null || token.scopes.any { it in scopes }) {
            return@intercept
        }

        val provider = context.attributes[OAuthAuthenticationProvider.KEY]
        val properties = mapOf(
            "error" to "insufficient_scope",
            "error_description" to "The request requires higher privileges than provided by the access token."
        )
        val header = HttpAuthHeader.bearerAuthChallenge(provider.realm, provider.schemes, properties)
        context.response.header(HttpHeaders.WWWAuthenticate, header.render())
        context.respond(object : OutgoingContent.NoContent() {
            override val status = HttpStatusCode.Forbidden
        })
        finish()
    }
    return route
}

/**
 * An authentication route node that is used by [OAuthAuthenticationProvider] server to specify the OAuth scopes
 * that are allowed to access this route and usually created by [Route.scoped] DSL function so generally there is no
 * need to instantiate it directly unless you are writing an extension
 */
class ScopedRouteSelector(val scopes: Set<String>) : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(scopes ${scopes.joinToString()}})"
}

/**
 * Create an OAuth 2 token endpoint route.
 *
 * @param server The OAuth server to use.
 */
fun <C : Principal, U : Principal> Route.oauthTokenEndpoint(server: OAuthServer<C, U>) {
    post {
        val contentType = call.request.contentType()
        val parameters = when {
            contentType.match(ContentType.Application.FormUrlEncoded) -> call.receiveParameters()
            else -> call.parameters
        }
        val state = parameters.state

        try {
            val grant = call.oauthGrant(server, parameters)
            call.respondText(grant.toJson(), ContentType.Application.Json)
        } catch (e: OAuthError) {
            application.log.debug("The grant request failed to process", e)
            call.respondText(e.toJson(state), ContentType.Application.Json, e.status)
        }
    }

    handle {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
}
