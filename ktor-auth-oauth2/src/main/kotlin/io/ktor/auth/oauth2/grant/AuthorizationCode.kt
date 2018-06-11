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

package io.ktor.auth.oauth2.grant

import io.ktor.application.ApplicationCall
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.InvalidClient
import io.ktor.auth.oauth2.InvalidRequest
import io.ktor.auth.oauth2.InvalidScope
import io.ktor.auth.oauth2.ServerError
import io.ktor.auth.oauth2.repository.AuthorizationCodeRepository
import io.ktor.auth.oauth2.util.getNonBlank
import io.ktor.auth.oauth2.util.redirectUri
import io.ktor.http.Parameters
import java.net.URI
import java.time.Duration
import java.time.Instant

/**
 * A representation of the authorization code given to the client to request an access token.
 *
 * @property code The string representation of the authorization code.
 * @property client The client principal to which the authorization code belongs
 * @property user The user on whose behalf the code was generated.
 * @property issuedAt The instant at which the code was issued.
 * @property expiresIn The duration after which the code expires.
 * @property redirectUri The redirect uri specified by the client.
 * @property scopes The requested scopes.
 * @property state The additional state property.
 */
data class AuthorizationCode<out C : Principal, out U : Principal>(
    val code: String,
    val client: C,
    val user: U,
    val issuedAt: Instant,
    val expiresIn: Duration?,
    val redirectUri: URI,
    val scopes: Set<String> = emptySet(),
    val state: String? = null
) {
    /**
     * Determine whether the [AuthorizationCode] is expired or not.
     */
    fun isExpired(): Boolean = expiresIn?.let { Instant.now().isAfter(issuedAt + it) } ?: false
}

/**
 * The Authorization Code grant is used when an application exchanges an authorization code for an access token.
 * After the user returns to the application via the redirect URL, the application will get the authorization code from
 * the URL and use it to request an access token. This request will be made to the token endpoint.
 *
 * See http://tools.ietf.org/html/rfc6749#section-4.1
 *
 * @property codeRepository The repository to generate the authorization codes.
 */
open class AuthorizationCodeGrantHandler<C : Principal, U : Principal>(
    val codeRepository: AuthorizationCodeRepository<C, U>
) : GrantHandler<C, U> {
    /**
     * The Authorization Code grant requires the client credentials to be passed to the token endpoint.
     */
    override val clientCredentialsRequired: Boolean = true

    override val supportsAuthorization: Boolean = true

    override val supportsGranting: Boolean = true

    override suspend fun AuthorizationRequest<C, U>.authorize(user: U): Authorization {
        val code = codeRepository.generate(this, user)
        return Authorization(parameters = Parameters.build {
            append("code", code.code)
            state?.let { append("state", it) }
        })
    }

    override suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U> {
        val server = request.server
        val client = request.client ?: throw ServerError()
        val redirectUri = request.parameters.redirectUri()
        val code = request.parameters.getNonBlank("code")?.let {
            codeRepository.lookup(it) ?: throw InvalidClient("The given authorization code is invalid.")
        } ?: throw InvalidRequest("The 'code' field is mandatory.")

        when {
            // Verify whether the client matches the principal of the authorization code
            client != code.client ->
                throw InvalidClient("The client credentials are invalid.")
            // Verify whether the client matches the principal of the authorization code
            redirectUri != code.redirectUri -> {
                throw InvalidClient("The redirect uri's do not match") }
        }

        val scopes = server.clientRepository.validateScopes(client, request.scopes) ?: throw InvalidScope("The requested scopes are not accepted.")

        // Generate token based on user principal
        val (token, refresh) = server.tokenRepository.generate(client, code.user, scopes)
        return Grant(accessToken = token, refreshToken = refresh, state = request.state)
    }
}
