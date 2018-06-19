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
import io.ktor.auth.oauth2.grant.AuthorizationCode
import io.ktor.auth.oauth2.grant.AuthorizationRequest
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * An [AuthorizationCodeRepository] that generates authorization codes with an in-memory database
 * for keeping track of the codes.
 *
 * @property validity The duration of the validity of the token.
 */
class MemoryAuthorizationCodeRepository<C : Principal, U : Principal>(
    val validity: Duration = Duration.ofMinutes(10)
) : AuthorizationCodeRepository<C, U> {
    /**
     * The active authorization codes.
     */
    private val active: MutableMap<UUID, AuthorizationCode<C, U>> = HashMap()

    override suspend fun lookup(code: String): AuthorizationCode<C, U>? {
        val uuid = try {
            UUID.fromString(code)
        } catch (e: IllegalArgumentException) {
            return null
        }

        // Remove all expired keys
        active
            .filterValues { it.isExpired() }
            .forEach { active.remove(it.key) }

        return active.remove(uuid)
    }

    override suspend fun generate(request: AuthorizationRequest<C, U>, user: U): AuthorizationCode<C, U> {
        val now = Instant.now()
        val code = UUID.randomUUID()
        return AuthorizationCode(
            code = code.toString(),
            client = request.client,
            user = user,
            issuedAt = now,
            expiresIn = validity,
            redirectUri = request.redirectUri,
            scopes = request.scopes,
            state = request.state
        ).also {
            active[code] = it
        }
    }
}
