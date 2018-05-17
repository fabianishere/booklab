/*
 * Copyright 2018 Fabian Mastenbroek.
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

import io.ktor.application.call
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

/**
 * An [AuthenticationProvider] for OAuth 2.0 authorization servers.
 *
 * @param name is the name of the server, or `null` for a default server
 */
class OAuthAuthenticationProvider<C : Principal, U : Principal>(name: String?) : AuthenticationProvider(name) {
    /**
     * The [OAuthServer] to use.
     */
    lateinit var server: OAuthServer<C, U>

    /**
     * Specifies realm to be passed in `WWW-Authenticate` header
     */
    var realm: String = "Ktor Server"

    /**
     * The scopes of this authentication server.
     */
    var scopes: Set<String> = emptySet()

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
}

/**
 * Install OAuth Authentication mechanism for an OAuth 2.0 authorization server.
 */
fun <C : Principal, U : Principal> Authentication.Configuration.oauth(
    name: String? = null,
    configure: OAuthAuthenticationProvider<C, U>.() -> Unit
) {
    val provider = OAuthAuthenticationProvider<C, U>(name).apply(configure)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val header: HttpAuthHeader? = call.request.parseAuthorizationHeaderOrNull()
        val token = header.getBlob(provider.schemes)?.let { provider.server.tokenRepository.lookup(it) }

        val cause = when {
            header == null -> AuthenticationFailedCause.NoCredentials
            token == null -> AuthenticationFailedCause.InvalidCredentials
            !token.scopes.any { it in provider.scopes }  -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(OAuthAuthKey, cause) {
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(provider.realm, provider.schemes)))
                it.complete()
            }
        } else if (token != null) {
            context.principal(token)
        }
    }

    register(provider)
}

private val OAuthAuthKey: Any = "OAuth"

internal class AuthSchemes(val defaultScheme: String, vararg val additionalSchemes: String) {
    val schemes = (arrayOf(defaultScheme) + additionalSchemes).toSet()
    val schemesLowerCase = schemes.map { it.toLowerCase() }.toSet()

    operator fun contains(scheme: String): Boolean = scheme.toLowerCase() in schemesLowerCase
}

private fun HttpAuthHeader?.getBlob(schemes: AuthSchemes) = when {
    this is HttpAuthHeader.Single && authScheme.toLowerCase() in schemes -> blob
    else -> null
}

private fun ApplicationRequest.parseAuthorizationHeaderOrNull() = try {
    parseAuthorizationHeader()
} catch (ex: IllegalArgumentException) {
    null
}

private fun HttpAuthHeader.Companion.bearerAuthChallenge(realm: String, schemes: AuthSchemes): HttpAuthHeader =
    HttpAuthHeader.Parameterized(schemes.defaultScheme, mapOf(HttpAuthHeader.Parameters.Realm to realm))
