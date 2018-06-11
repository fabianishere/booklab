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

package io.ktor.auth.oauth2.repository

import io.ktor.auth.Principal
import io.ktor.auth.oauth2.AccessToken

/**
 * A repository for generating and validating refresh tokens.
 */
interface RefreshTokenRepository<C : Principal, U : Principal> {
    /**
     * Generate a refresh token for the given [AccessToken].
     *
     * @param token The [AccessToken] to generate the refresh token for.
     */
    suspend fun generate(token: AccessToken<C, U>): String

    /**
     * Refresh an access token.
     *
     * @param token The refresh token to use.
     * @return The refresh access token or `null` if the refresh token is invalid or expired.
     */
    suspend fun refresh(token: String): AccessToken<C, U>
}
