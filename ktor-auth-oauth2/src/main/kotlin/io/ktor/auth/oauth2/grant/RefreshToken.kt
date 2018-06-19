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
import io.ktor.auth.oauth2.util.getNonBlank

/**
 * The Refresh Token grant type is used by clients to exchange a refresh token for an access token when the access token
 * has expired.
 *
 * This allows clients to continue to have a valid access token without further interaction with the user.
 *
 * See http://tools.ietf.org/html/rfc6749#section-4.3
 */
open class RefreshTokenGrantHandler<C : Principal, U : Principal> : GrantHandler<C, U> {
    override val clientCredentialsRequired: Boolean = true

    override val supportsAuthorization: Boolean = false

    override val supportsGranting: Boolean = true

    override suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U> {
        val server = request.server
        val refresh = request.parameters.getNonBlank("refresh_token") ?: throw InvalidRequest("The 'refresh_token' field is mandatory.")
        val (token, newRefresh) = server.tokenRepository.refresh(refresh) ?: throw InvalidClient("The refresh token is invalid.")
        return Grant(accessToken = token, refreshToken = newRefresh, state = request.state)
    }
}
