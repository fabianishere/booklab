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
import io.ktor.auth.oauth2.InvalidRequest
import io.ktor.auth.oauth2.InvalidScope
import io.ktor.http.Parameters

/**
 * The Implicit grant type is a simplified flow that can be used by public clients, where the access token is returned
 * immediately without an extra authorization code exchange step.
 *
 * See http://tools.ietf.org/html/rfc6749#section-4.2
 */
open class ImplicitGrantHandler<C : Principal, U : Principal> : GrantHandler<C, U> {
    override val clientCredentialsRequired: Boolean = true

    override val supportsAuthorization: Boolean = true

    override val supportsGranting: Boolean = true

    override suspend fun AuthorizationRequest<C, U>.authorize(user: U): Authorization {
        val scopes = server.clientRepository.validateScopes(client, scopes) ?: throw InvalidScope("The requested scopes are not accepted.")
        val (token, _) = server.tokenRepository.generate(client, user, scopes)
        return Authorization(parameters = Parameters.build {
            append("token_type", token.type)
            append("access_token", token.token)
            append("scope", scopes.joinToString(" "))
            token.expiresIn?.let { append("expires_in", it.seconds.toString()) }
            state?.let { append("state", it) }
        })
    }

    override suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U> {
        throw InvalidRequest("This request is not supported for the given grant type.")
    }
}
