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

package io.ktor.auth.oauth2.repository

import io.ktor.auth.Principal
import io.ktor.auth.oauth2.AccessToken

/**
 * A repository that allows the server to generate and validate access tokens.
 */
interface AccessTokenRepository<C : Principal, U : Principal> {
    /**
     * Lookup the given token and convert the string to an [AccessToken].
     *
     * @param token The token to convert to an object.
     * @return The [AccessToken] instance or `null` if the string is not a valid access token.
     */
    suspend fun lookup(token: String): AccessToken<C, U>?

    /**
     * Generate a new [AccessToken] for the given client acting on behalf of the given user.
     *
     * @param client The client for which the access token is generated.
     * @param user The user associated with the access token.
     * @param scopes The scopes to which the access token applies.
     * @return A pair containing the generated access token and the refresh token.
     */
    suspend fun generate(client: C, user: U? = null, scopes: Set<String> = emptySet()): Pair<AccessToken<C, U>, String?>

    /**
     * Refresh an access token.
     *
     * @param token The refresh token to use.
     * @return The refresh access token or `null` if the refresh token is invalid or expired.
     */
    suspend fun refresh(token: String): Pair<AccessToken<C, U>, String?>?
}
