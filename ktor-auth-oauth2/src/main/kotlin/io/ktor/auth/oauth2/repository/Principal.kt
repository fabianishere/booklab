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

import io.ktor.auth.Credential
import io.ktor.auth.Principal

/**
 * A repository for finding and validating principals based on credentials.
 */
interface PrincipalRepository<in C : Credential, P : Principal> {
    /**
     * A unique identifier for the client principal.
     */
    val P.id: String

    /**
     * Lookup a principal by its unique identifier.
     *
     * @return The principal that has been found or `null` if it does not exists.
     */
    suspend fun lookup(id: String): P?

    /**
     * Validate the credentials of a principal.
     *
     * @param credential The credentials of the principal.
     * @param authorize A flag to indicate this validation step runs during the authorization procedure, which does not
     * require the presence of a password.
     * @return The principal that has been validated or `null` if it failed to validate.
     */
    suspend fun validate(credential: C, authorize: Boolean = false): P?
}
