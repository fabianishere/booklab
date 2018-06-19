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
import io.ktor.auth.oauth2.AccessToken
import io.ktor.auth.oauth2.OAuthError
import io.ktor.auth.oauth2.OAuthServer
import io.ktor.auth.oauth2.ServerError
import io.ktor.http.Parameters
import java.net.URI

/**
 * A representation of a request for a [Grant] by some client.
 *
 * @property server The server that has generated the request.
 * @property handler The handler assigned to this request.
 * @property client The client principal that is requesting the grant.
 * @property scopes The requested scopes.
 * @property state The additional state property.
 * @property parameters The additional parameters passed to the server.
 */
data class GrantRequest<C : Principal, U : Principal>(
    internal val server: OAuthServer<C, U>,
    internal val handler: GrantHandler<C, U>,
    val client: C?,
    val scopes: Set<String> = emptySet(),
    val state: String? = null,
    val parameters: Parameters = Parameters.Empty
)

/**
 * An authorization grant returned by the authorization server.
 *
 * @property accessToken The granted access token
 * @property refreshToken The token to request a new access token.
 * @property state The additional state property.
 */
data class Grant<out C : Principal, out U : Principal>(
    val accessToken: AccessToken<C, U>,
    val refreshToken: String? = null,
    val state: String? = null
)

/**
 * An authorization request send to the "/authorize" endpoint of the authorization server.
 *
 * @property server The server that has generated the request.
 * @property handler The handler assigned to this request.
 * @property client The client that has sent this request.
 * @property redirectUri The uri to redirect to.
 * @property scopes The requested scopes.
 * @property state The state property.
 */
data class AuthorizationRequest<C : Principal, U : Principal>(
    internal val server: OAuthServer<C, U>,
    internal val handler: GrantHandler<C, U>,
    val client: C,
    val redirectUri: URI,
    val scopes: Set<String> = emptySet(),
    val state: String? = null
)

/**
 * An authorization response returned when the client was authorized.
 *
 * @property parameters The parameters of the response.
 */
data class Authorization(val parameters: Parameters = Parameters.Empty)

/**
 * A handler interface for the multiple grant types supported by the OAuth 2.0 specification.
 */
interface GrantHandler<C : Principal, U : Principal> {
    /**
     * A flag to indicate whether client credentials are required for issuing grants with this [GrantHandler].
     */
    val clientCredentialsRequired: Boolean

    /**
     * A flag to indicate whether this [GrantHandler] supports the authorization endpoint.
     */
    val supportsAuthorization: Boolean

    /**
     * A flag to indicate whether this [GrantHandler] supports a token grant endpoint.
     */
    val supportsGranting: Boolean

    /**
     * Authorize the given client and return the proper url to redirect to.
     *
     * @param user The user to authorize the request.
     * @return The proper url to redirect to.
     * @throws OAuthError if the authorization failed.
     */
    suspend fun AuthorizationRequest<C, U>.authorize(user: U): Authorization {
        throw ServerError("The authorization endpoint is not supported for this grant type.")
    }

    /**
     * Handle the grant request over HTTP and returns a [Grant].
     *
     * @param server The server that is handling the request.
     * @param request The request to handle.
     * @throws OAuthError if the grant request failed to process.
     */
    suspend fun ApplicationCall.grant(request: GrantRequest<C, U>): Grant<C, U>
}
