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
import io.ktor.auth.UserPasswordCredential
import io.ktor.auth.oauth2.InvalidClient
import io.ktor.auth.oauth2.InvalidGrant
import io.ktor.auth.oauth2.InvalidScope
import io.ktor.auth.oauth2.ServerError
import io.ktor.auth.oauth2.repository.PrincipalRepository
import io.ktor.auth.oauth2.util.getNonBlank

/**
 * The Password grant is used when the application exchanges the user’s username and password for an access token.
 * This is exactly the thing OAuth was created to prevent in the first place, so you should never allow third-party
 * apps to use this grant.
 *
 * See http://tools.ietf.org/html/rfc6749#section-4.3
 *
 * @property repository The user repository to use.
 */
open class PasswordGrantHandler<C : Principal, U : Principal>(
    val repository: PrincipalRepository<UserPasswordCredential, U>
) : GrantHandler<C, U> {
    override val clientCredentialsRequired: Boolean = true

    override val supportsAuthorization: Boolean = false

    override val supportsGranting: Boolean = true

    override suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U> {
        val server = request.server
        val client = request.client ?: throw ServerError()
        val credentials = let {
            val username = request.parameters.getNonBlank("username") ?: throw InvalidClient("No 'username' parameter in request.")
            val password = request.parameters.getNonBlank("password") ?: throw InvalidClient("No 'password' parameter in request.")

            UserPasswordCredential(username, password)
        }

        val scopes = server.clientRepository.validateScopes(client, request.scopes) ?: throw InvalidScope("The requested scopes are not accepted.")
        val user = repository.validate(credentials) ?: throw InvalidGrant("Invalid user credentials")

        // Generate token based on user principal
        val (token, refresh) = server.tokenRepository.generate(client, user, scopes)
        return Grant(accessToken = token, refreshToken = refresh, state = request.state)
    }
}
