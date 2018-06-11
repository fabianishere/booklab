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
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProvider
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.Principal
import io.ktor.auth.UnauthorizedResponse
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.request.ApplicationRequest
import io.ktor.response.respond
import io.ktor.util.AttributeKey

/**
 * An [AuthenticationProvider] for the Ktor OAuth2 Authorization Server implementation.
 *
 * @param name is the name of the server, or `null` for a default server
 * @property server The [OAuthServer] to use for the authorizing clients.
 */
class OAuthAuthenticationProvider<C : Principal, U : Principal>(name: String? = null, val server: OAuthServer<C, U>) : AuthenticationProvider(name) {
    /**
     * Specifies realm to be passed in `WWW-Authenticate` header
     */
    var realm: String = "Ktor Server"

    /**
     * The authentication schemes to use.
     */
    internal var schemes = AuthSchemes("Bearer")

    /**
     * Configure the supported authentication schemes that are accepted by the server.
     *
     * @param [defaultScheme] default scheme that will be used to challenge the client when no valid auth is provided
     * @param [additionalSchemes] additional schemes that will be accepted when validating the authentication
     */
    fun authSchemes(defaultScheme: String = "Bearer", vararg additionalSchemes: String) {
        schemes = AuthSchemes(defaultScheme, *additionalSchemes)
    }

    companion object {
        /**
         * An [AttributeKey] that provides access to the [OAuthAuthenticationProvider] in the current [ApplicationCall].
         */
        internal val KEY = AttributeKey<OAuthAuthenticationProvider<*, *>>("OAuthProvider")
    }
}

/**
 * Install OAuth Authentication mechanism for the given [Authentication.Configuration] instance.
 *
 * @param name The name of the authentication server (or `null` for default).
 * @param configure A block to configure the server.
 */
fun <C : Principal, U : Principal> Authentication.Configuration.oauth(
    server: OAuthServer<C, U>,
    name: String? = null,
    configure: OAuthAuthenticationProvider<C, U>.() -> Unit = {}
) {
    val provider = OAuthAuthenticationProvider<C, U>(name, server).apply(configure)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val call = context.call
        val header: HttpAuthHeader? = call.request.parseAuthorizationHeaderOrNull()
        val token = header.getBlob(provider.schemes)?.let { server.tokenRepository.lookup(it) }

        // If the request lacks any authentication information (e.g., the client was unaware that authentication is
        // necessary or attempted using an unsupported authentication method), the resource server SHOULD NOT
        // include an error code or other error information. See https://tools.ietf.org/html/rfc6750#section-3.1
        if (header == null) {
            context.challenge(OAuthAuthKey, AuthenticationFailedCause.NoCredentials) {
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(provider.realm, provider.schemes)))
                it.complete()
            }
        } else if (token == null) {
            context.challenge(OAuthAuthKey, AuthenticationFailedCause.InvalidCredentials) {
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(provider.realm, provider.schemes)))
                it.complete()
            }
        } else {
            // XXX Hacky way to get access to the authentication server in the routes
            // In the future, we should have a proper way to access the matched authentication server
            call.attributes.put(OAuthAuthenticationProvider.KEY, provider)
            context.principal(token)
        }
    }

    register(provider)
}

/**
 * The key we use to challenge the request.
 */
private val OAuthAuthKey: Any = "OAuth"

internal class AuthSchemes(val defaultScheme: String, vararg val additionalSchemes: String) {
    val schemes = (arrayOf(defaultScheme) + additionalSchemes).toSet()
    val schemesLowerCase = schemes.map { it.toLowerCase() }.toSet()

    operator fun contains(scheme: String): Boolean = scheme.toLowerCase() in schemesLowerCase
}

/**
 * Return the blob inside the HTTP Authorization header.
 *
 * @param schemes The supported authorization schemes.
 * @return The blob inside the authorization header.
 */
private fun HttpAuthHeader?.getBlob(schemes: AuthSchemes) = when {
    this is HttpAuthHeader.Single && authScheme.toLowerCase() in schemes -> blob
    else -> null
}

/**
 * Parse the Authorization header of the request or return `null` when there is no such header in the request.
 */
private fun ApplicationRequest.parseAuthorizationHeaderOrNull() = try {
    parseAuthorizationHeader()
} catch (ex: IllegalArgumentException) {
    null
}

/**
 * Construct a Bearer Authentication challenge.
 *
 * @param realm The authentication realm.
 * @param schemes The supported authentication schemes.
 * @param properties The additional properties to give.
 */
internal fun HttpAuthHeader.Companion.bearerAuthChallenge(
    realm: String,
    schemes: AuthSchemes,
    properties: Map<String, String> = emptyMap()
): HttpAuthHeader =
    HttpAuthHeader.Parameterized(
        schemes.defaultScheme,
        mapOf(HttpAuthHeader.Parameters.Realm to realm).plus(properties)
    )
