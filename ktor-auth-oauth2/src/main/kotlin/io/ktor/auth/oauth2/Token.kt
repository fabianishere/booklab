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

import io.ktor.auth.Principal
import java.time.Duration
import java.time.Instant

/**
 * A representation of some access token granted by the Authorization Server.
 */
interface AccessToken<out C : Principal, out U : Principal> : Principal {
    /**
     * A string representation of the type of token.
     */
    val type: String

    /**
     * The string representation of the access token.
     */
    val token: String

    /**
     * The client to which the token belongs.
     */
    val client: C

    /**
     * The user on whose behalf the token is given.
     */
    val user: U?

    /**
     * The time at which the token was issued.
     */
    val issuedAt: Instant

    /**
     * The duration after which the token expires.
     */
    val expiresIn: Duration?

    /**
     * The requested scopes of the access token.
     */
    val scopes: Set<String>

    /**
     * Determine whether the [AccessToken] is expired or not.
     */
    fun isExpired(): Boolean = expiresIn?.let { Instant.now().isAfter(issuedAt + it) } ?: false
}
