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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.AccessToken
import java.time.Duration
import java.time.Instant
import java.util.Date

/**
 * The configuration for generating JWT tokens.
 */
class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val algorithm: Algorithm
)

/**
 * An [AccessTokenRepository] that generates stateless JWT tokens which do not require the tokens to be persisted.
 *
 * @property configuration The configuration used for generating the JWT tokens.
 * @property userRepository The repository for looking up and validating user principals.
 * @property clientRepository The repository for looking up and validating client principals.
 * @property refreshTokenRepository The repository for generating refresh tokens.
 * @property validity The duration of the validity of the token.
 */
class JwtAccessTokenRepository<C : Principal, U : Principal>(
    val configuration: JwtConfiguration,
    val userRepository: UserRepository<U>,
    val clientRepository: ClientRepository<C>,
    val refreshTokenRepository: RefreshTokenRepository<C, U>? = null,
    val validity: Duration? = Duration.ofMinutes(15)
) : AccessTokenRepository<C, U> {

    private val verifier = JWT.require(configuration.algorithm)
        .withIssuer(configuration.issuer)
        .withAudience(configuration.audience)
        .withSubject("access-token")
        .build()

    override suspend fun lookup(token: String): AccessToken<C, U>? {
        val jwt = try {
            verifier.verify(token)
        } catch (e: JWTVerificationException) {
            return null
        }

        val client = jwt.getClaim("client")?.let { clientRepository.lookup(it.asString()) } ?: return null
        val user = jwt.getClaim("user")?.asString()?.let { userRepository.lookup(it) }
        return object : AccessToken<C, U> {
            override val type: String = "Bearer"
            override val token: String = token
            override val client: C = client
            override val user: U? = user
            override val issuedAt: Instant = jwt.issuedAt.toInstant()
            override val expiresIn: Duration? = jwt.expiresAt?.toInstant()?.let { Duration.between(issuedAt, it) }
            override val scopes: Set<String> = jwt.getClaim("scopes")?.asList(String::class.java)?.toSet() ?: emptySet()
        }
    }

    override suspend fun generate(client: C, user: U?, scopes: Set<String>): Pair<AccessToken<C, U>, String?> {
        val now = Instant.now()
        val jwt = JWT.create()
            .withIssuer(configuration.issuer)
            .withAudience(configuration.audience)
            .withSubject("access-token")
            .withIssuedAt(Date.from(now))
            .withClaim("client", clientRepository.run { client.id })
            .apply {
                if (validity != null)
                    withExpiresAt(Date.from(now + validity))
                if (user != null)
                    withClaim("user", userRepository.run { user.id })
                if (scopes.isNotEmpty())
                    withArrayClaim("scopes", scopes.toTypedArray())
            }
            .sign(configuration.algorithm)
        val token = object : AccessToken<C, U> {
            override val type: String = "Bearer"
            override val token: String = jwt
            override val client: C = client
            override val user: U? = user
            override val issuedAt: Instant = now
            override val expiresIn: Duration? = validity
            override val scopes: Set<String> = scopes
        }
        return Pair(token, refreshTokenRepository?.generate(token))
    }

    override suspend fun refresh(token: String): Pair<AccessToken<C, U>, String?>? {
        if (refreshTokenRepository != null) {
            val access = refreshTokenRepository.refresh(token)
            val refresh = refreshTokenRepository.generate(access)
            return Pair(access, refresh)
        }
        return null
    }
}
