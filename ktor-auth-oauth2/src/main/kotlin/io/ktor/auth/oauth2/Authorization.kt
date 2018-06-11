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
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.grant.Authorization
import io.ktor.auth.oauth2.grant.AuthorizationRequest
import io.ktor.auth.oauth2.util.clientCredentials
import io.ktor.auth.oauth2.util.redirectUri
import io.ktor.auth.oauth2.util.responseType
import io.ktor.auth.oauth2.util.scopes
import io.ktor.auth.oauth2.util.state
import io.ktor.http.Parameters

/**
 * Receive an OAuth 2.0 authorization request.
 *
 * @param server The OAuth server server to use.
 * @param params The parameters to use.
 * @return The authorization request.
 * @throws OAuthError if the authorization failed.
 */
suspend fun <C : Principal, U : Principal> ApplicationCall.getAuthorizationRequest(
    server: OAuthServer<C, U>,
    params: Parameters = parameters
): AuthorizationRequest<C, U> {
    // We should make this more configurable in the future
    val responseType = params.responseType ?: throw InvalidRequest("The 'response_type' field is mandatory.")
    val handler = when (responseType) {
        "code" -> server.handlers["authorization_code"]
        "token" -> server.handlers["implicit"]
        else -> server.handlers[responseType]
    }

    if (handler == null || !handler.supportsAuthorization) {
        throw UnsupportedResponseType("The response type '$responseType' is not supported.")
    }

    val credential = params.clientCredentials()?.copy(secret = null) ?: throw InvalidRequest("The client credentials are required.")
    val client = server.clientRepository.validate(credential, authorize = true) ?: throw InvalidClient("The client credentials are invalid.")
    val redirectUri = params.redirectUri()
    val scopes = params.scopes ?: server.defaultScopes
    val state = params.state

    return when {
        redirectUri == null ->
            throw InvalidRequest("The 'redirect_uri' field is mandatory.")
        !server.clientRepository.validateRedirectUri(client, redirectUri) ->
            throw InvalidClient("The redirect uri is invalid.")
        server.clientRepository.validateScopes(client, scopes) == null ->
            throw InvalidScope("The requested scopes are not accepted.")
        else ->
            AuthorizationRequest(server, handler, client, redirectUri, scopes, state)
    }
}

/**
 * Authorize the client for the given user.
 *
 * @param user The user to authorize on behalf of.
 * @return The url to redirect to.
 * @throws OAuthError if the authorization failed.
 */
suspend fun <C : Principal, U : Principal> AuthorizationRequest<C, U>.authorize(user: U): Authorization =
    this.handler.run { authorize(user) }
