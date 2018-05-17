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

import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.Principal
import io.ktor.auth.basicAuthenticationCredentials
import io.ktor.auth.oauth2.grant.Authorization
import io.ktor.auth.oauth2.grant.AuthorizationRequest
import io.ktor.auth.oauth2.grant.Grant
import io.ktor.auth.oauth2.grant.GrantHandler
import io.ktor.auth.oauth2.grant.GrantRequest
import io.ktor.auth.oauth2.repository.AccessTokenRepository
import io.ktor.auth.oauth2.repository.ClientCredential
import io.ktor.auth.oauth2.repository.ClientRepository
import io.ktor.auth.oauth2.util.toJson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.contentType
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import java.net.URI
import java.net.URISyntaxException

/**
 * An OAuth 2.0 authorization server for the Ktor web framework.
 *
 * @property handlers A mapping from grant types to its respective handler.
 * @property clientRepository The repository responsible for managing clients.
 * @property tokenRepository The repository responsible for generating tokens.
 */
class OAuthServer<C : Principal, U : Principal>(
    val handlers: Map<String, GrantHandler<C, U>>,
    val clientRepository: ClientRepository<C>,
    val tokenRepository: AccessTokenRepository<C, U>
)

/**
 * Create an OAuth 2 token endpoint route.
 *
 * @param server The [OAuthServer] to use.
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
            application.log.error("The grant request failed to process", e)
            call.respondText(e.toJson(state), ContentType.Application.Json, e.status)
        }
    }

    handle {
        call.respond(HttpStatusCode.MethodNotAllowed)
    }
}
/**
 * Handle the given [ApplicationCall] as an OAuth 2.0 authorization grant request and return the authorization
 * [Grant].
 *
 * @param server The OAuth server provider to use.
 * @param params The parameters to use.
 * @return The authorization [Grant].
 * @throws OAuthError if the authorization failed.
 */
suspend fun <C : Principal, U : Principal> ApplicationCall.oauthGrant(
    server: OAuthServer<C, U>,
    params: Parameters = Parameters.Empty
): Grant<C, U> {
    val grantType = params.grantType ?: throw InvalidRequest("The 'grant_type' field is mandatory.")
    val handler = server.handlers[grantType] ?: throw UnsupportedGrantType("The grant type $grantType is not supported.")
    val scope = params.scope

    // Parse the credentials from either the request or from the Http Authentication header.
    val credentials = request.basicAuthenticationCredentials()
        ?.let { ClientCredential(it.name, it.password) }
        ?: params.clientCredentials()

    val principal = credentials?.let { server.clientRepository.validate(it) }

    if (handler.clientCredentialsRequired) {
        when {
            credentials == null ->
                throw InvalidRequest("The client credentials are required for grant type $grantType.")
            principal == null ->
                throw InvalidClient("The client credentials are not accepted.")
        }
    }

    val request = GrantRequest(server, handler, principal, scope, params.state, params)
    return handler.run { grant(request) }
}

/**
 * Receive an OAuth 2.0 authorization request.
 *
 * @param server The OAuth server provider to use.
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
    val scope = params.scope
    val state = params.state

    return when {
        redirectUri == null ->
            throw InvalidRequest("The 'redirect_uri' field is mandatory.")
        !server.clientRepository.validateRedirectUri(client, redirectUri) ->
            throw InvalidClient("The redirect uri is invalid.")
        !server.clientRepository.validateScope(client, scope) ->
            throw InvalidScope("The requested scope is not accepted.")
        else ->
            AuthorizationRequest(server, handler, client, redirectUri, scope, state)
    }
}

/**
 * Authorize the client for the given user.
 *
 * @param server The OAuth server provider to use.
 * @param user The user to authorize on behalf of.
 * @return The url to redirect to.
 * @throws OAuthError if the authorization failed.
 */
suspend fun <C : Principal, U : Principal> AuthorizationRequest<C, U>.authorize(user: U): Authorization =
    this.handler.run { authorize(user) }

/**
 * Read a non-blank value from the parameters.
 */
internal fun Parameters.getNonBlank(key: String) = this[key]?.takeUnless { it.isBlank() }

/**
 * Parse the redirect [URI] from the parameters.
 *
 * @return The parsed redirect uri.
 */
fun Parameters.redirectUri(): URI? = try {
    getNonBlank("redirect_uri")?.let { URI(it) }
} catch (e: URISyntaxException) {
    throw InvalidRequest("The given request uri is invalid.")
}

/**
 * The type of the grant read from the parameters.
 *
 * 3.2: Parameters sent without a value MUST be treated as if they were omitted from the request.
 * The authorization server MUST ignore unrecognized request parameters.  Request and response parameters
 * MUST NOT be included more than once.
 */
val Parameters.grantType: String? get() = getNonBlank("grant_type")

/**
 * The requested response type.
 */
val Parameters.responseType: String? get() = getNonBlank("response_type")

/**
 * The grant scope from the parameters.
 */
val Parameters.scope: String? get() = getNonBlank("scope")

/**
 * The optional state parameter.
 */
val Parameters.state: String? get() = getNonBlank("state")

/**
 * Parse the client credentials from the [Parameters] instance.
 *
 * @return The client credentials passed in the parameters or `null` if the credentials were not found.
 */
internal fun Parameters.clientCredentials(): ClientCredential? {
    val id = getNonBlank("client_id")
    val secret = getNonBlank("client_secret")

    // Note that per the OAuth Specification, a Client may be valid if it only contains a client ID but no client
    // secret (common with Public Clients). However, if the registered client has a client secret value the specification
    // requires that a client secret must always be provided and verified for that client ID.
    if (id != null)
        return ClientCredential(id, secret)
    else
        return null
}
