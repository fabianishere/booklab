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
import io.ktor.auth.oauth2.grant.AuthorizationCode
import io.ktor.auth.oauth2.grant.AuthorizationRequest

/**
 * A repository that is responsible for generating and looking up authorization codes.
 */
interface AuthorizationCodeRepository<C : Principal, U : Principal> {
    /**
     * Lookup the given authorization code and convert the result into an [AuthorizationCode] instance.
     *
     * @param code The string representation of a code to lookup.
     * @return The [AuthorizationCode] instance or `null` if the code is not valid.
     */
    suspend fun lookup(code: String): AuthorizationCode<C, U>?

    /**
     * Generate an [AuthorizationCode] for the given [AuthorizationRequest] on behalf of the specified user.
     *
     * @param request The request to create an authorization code for.
     * @param user The user to generate the code on behalf of.
     * @return The generated authorization code.
     */
    suspend fun generate(request: AuthorizationRequest<C, U>, user: U): AuthorizationCode<C, U>
}
