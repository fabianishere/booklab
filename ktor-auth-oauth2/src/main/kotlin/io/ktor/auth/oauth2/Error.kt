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

import io.ktor.http.HttpStatusCode

/**
 * The base class of all OAuth 2 errors.
 *
 * @property type The string representation of the type of error.
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
sealed class OAuthError(
    val type: String,
    description: String,
    val status: HttpStatusCode = HttpStatusCode.BadRequest
) : Exception(description)

/**
 * The request is missing a required parameter, includes an unsupported parameter value (other than grant type),
 * repeats a parameter, includes multiple credentials, utilizes more than one mechanism for authenticating the
 * client, or is otherwise malformed.
 */
class InvalidRequest(description: String = "") : OAuthError("invalid_request", description)

/**
 * Client authentication failed (e.g., unknown client, no client authentication included, or unsupported
 * authentication method).  The authorization server MAY return an HTTP 401 (Unauthorized) status code to indicate
 * which HTTP authentication schemes are supported.  If the client attempted to authenticate via the "Authorization"
 * request header field, the authorization server MUST respond with an HTTP 401 (Unauthorized) status code and
 * include the "WWW-Authenticate" response header field matching the authentication scheme used by the client.
 */
class InvalidClient(description: String = "") : OAuthError("invalid_client", description, HttpStatusCode.Unauthorized)

/**
 * The provided authorization grant (e.g., authorization code, resource owner credentials) or refresh token is
 * invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to
 * another client.
 */
class InvalidGrant(description: String = "") : OAuthError("invalid_grant", description)

/**
 * The authenticated client is not authorized to use this authorization grant type.
 */
class UnauthorizedClient(description: String = "") : OAuthError("unauthorized_client", description)

/**
 * The authorization grant type is not supported by the authorization server.
 */
class UnsupportedGrantType(description: String = "") : OAuthError("unsupported_grant_type", description)

/**
 * The requested scope is invalid, unknown, malformed, or exceeds the scope granted by the resource owner.
 */
class InvalidScope(description: String = "") : OAuthError("invalid_scope", description)

/**
 * The resource owner or authorization server denied the request.
 */
class AccessDenied(description: String = "") : OAuthError("access_denied", description, HttpStatusCode.Unauthorized)

/**
 * The authorization server does not support obtaining an authorization code using this method.
 */
class UnsupportedResponseType(description: String = "") : OAuthError("unsupported_response_type", description)

/**
 * An internal server error occurred.
 */
class ServerError(description: String = "") : OAuthError("server_error", description, HttpStatusCode.InternalServerError)
