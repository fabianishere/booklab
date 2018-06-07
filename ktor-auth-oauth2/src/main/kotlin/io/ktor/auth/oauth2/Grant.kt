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
import io.ktor.auth.HttpAuthHeader
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.grant.Grant
import io.ktor.auth.oauth2.grant.GrantRequest
import io.ktor.auth.oauth2.repository.ClientCredential
import io.ktor.auth.oauth2.util.clientCredentials
import io.ktor.auth.oauth2.util.grantType
import io.ktor.auth.oauth2.util.scopes
import io.ktor.auth.oauth2.util.state
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.Parameters
import io.ktor.request.ApplicationRequest
import java.util.Base64

/**
 * Handle the given [ApplicationCall] as an OAuth 2.0 authorization grant request and return the authorization
 * [Grant].
 *
 * @param server The OAuth server to use.
 * @param params The parameters to use.
 * @return The authorization [Grant].
 * @throws OAuthError if the authorization failed.
 */
suspend fun <C : Principal, U : Principal> ApplicationCall.oauthGrant(
    server: OAuthServer<C, U>,
    params: Parameters = Parameters.Empty
): Grant<C, U> {
    val grantType = params.grantType ?: throw InvalidRequest("The 'grant_type' field is mandatory.")
    val handler = server.handlers[grantType]

    if (handler == null || !handler.supportsGranting) {
        throw UnsupportedGrantType("The grant type $grantType is not supported.")
    }

    val scopes = params.scopes ?: server.defaultScopes

    // Parse the credentials from either the request or from the Http Authentication header.
    val credentials = request.basicAuthenticationClientCredentials()
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

    val request = GrantRequest(server, handler, principal, scopes, params.state, params)
    return handler.run { grant(request) }
}

/**
 * Parse the client credentials from the Authorization Header in the [ApplicationRequest].
 */
fun ApplicationRequest.basicAuthenticationClientCredentials(): ClientCredential? {
    val parsed = parseAuthorizationHeader()
    when (parsed) {
        is HttpAuthHeader.Single -> {
            // Verify the auth scheme is HTTP Basic. According to RFC 2617, the authorization scheme should not be case
            // sensitive; thus BASIC, or Basic, or basic are all valid.
            if (!parsed.authScheme.equals("Basic", ignoreCase = true)) {
                return null
            }

            // here we can only use ISO 8859-1 character encoding because there is no character encoding specified as per RFC
            //     see http://greenbytes.de/tech/webdav/draft-reschke-basicauth-enc-latest.html
            //      http://tools.ietf.org/html/draft-ietf-httpauth-digest-15
            //      https://bugzilla.mozilla.org/show_bug.cgi?id=41489
            //      https://code.google.com/p/chromium/issues/detail?id=25790
            val userPass = try {
                Base64.getDecoder().decode(parsed.blob).toString(Charsets.ISO_8859_1)
            } catch (e: IllegalArgumentException) {
                return null
            }

            if (":" !in userPass) {
                return null
            }

            return ClientCredential(userPass.substringBefore(":"), userPass.substringAfter(":"))
        }
        else -> return null
    }
}
