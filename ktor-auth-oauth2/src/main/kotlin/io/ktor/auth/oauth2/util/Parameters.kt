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

package io.ktor.auth.oauth2.util

import io.ktor.auth.oauth2.InvalidRequest
import io.ktor.auth.oauth2.repository.ClientCredential
import io.ktor.http.Parameters
import java.net.URI
import java.net.URISyntaxException

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
 * The grant scopes from the parameters.
 */
val Parameters.scopes: Set<String>? get() = getNonBlank("scope")?.split(" ")?.toSet()

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
