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

package nl.tudelft.booklab.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.util.AttributeKey
import java.time.Duration

/**
 * The configuration for the JWT authentication method.
 */
data class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val realm: String,
    val validity: Duration,
    val algorithm: Algorithm
) {

    /**
     * The [JWTVerifier] instance to use.
     */
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .withSubject("access-token")
        .build()

    /**
     * The JWT builder instance to create the tokens with.
     */
    val creator: JWTCreator.Builder = JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withSubject("access-token")

    companion object {
        /**
         * The attribute key that allows the user to access the [JwtConfiguration] object within an application.
         */
        val KEY = AttributeKey<JwtConfiguration>("JwtConfiguration")
    }
}

/**
 * Build the [JwtConfiguration] object for the given configuration environment.
 *
 * @return The [JwtConfiguration] instance that has been built.
 */
fun ApplicationConfig.asJwtConfiguration(): JwtConfiguration {
    val issuer = property("domain").getString()
    val audience = property("audience").getString()
    val realm = property("realm").getString()
    val passphrase = property("passphrase").getString()
    val validity = Duration.parse(property("validity").getString())

    return JwtConfiguration(issuer, audience, realm, validity, Algorithm.HMAC512(passphrase))
}

/**
 * Extension method for accessing the [JwtConfiguration] instance of the [Application].
 */
val Application.jwt: JwtConfiguration get() = attributes[JwtConfiguration.KEY]
