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

package io.ktor.auth.oauth2.grant

import io.ktor.application.ApplicationCall
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.InvalidScope
import io.ktor.auth.oauth2.ServerError

/**
 * The Client Credentials grant is used when applications request an access token to access their own resources, not on
 * behalf of a user.
 *
 * See http://tools.ietf.org/html/rfc6749#section-4.4
 */
open class ClientCredentialsGrantHandler<C : Principal, U : Principal> : GrantHandler<C, U> {
    /**
     * Map the credentials of a client to a user principal. This method can be optionally overridden to map a client to
     * a user.
     *
     * @param client The client to map to a user.
     * @return The user associated with this client.
     */
    @Suppress("unused")
    suspend fun ApplicationCall.authenticate(client: C): U? = null

    override val clientCredentialsRequired: Boolean = true

    override val supportsAuthorization: Boolean = false

    override suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U> {
        val server = request.server
        val client = request.client ?: throw ServerError()

        val scopes = server.clientRepository.validateScopes(client, request.scopes) ?: throw InvalidScope("The requested scopes are not accepted.")

        // Generate token based on client principal
        val (token, refresh) = server.tokenRepository.generate(client, authenticate(client), scopes = scopes)
        return Grant(accessToken = token, refreshToken = refresh, state = request.state)
    }
}
