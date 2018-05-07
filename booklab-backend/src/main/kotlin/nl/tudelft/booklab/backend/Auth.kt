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

package nl.tudelft.booklab.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.AuthenticationContext
import io.ktor.util.AttributeKey
import java.time.Duration

/**
 * The configuration for the JWT authentication method.
 */
class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val realm: String,
    val duration: Duration,
    val algorithm: Algorithm
) {

    /**
     * The [JWTVerifier] instance to use.
     */
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    /**
     * The JWT builder instance to create the tokens with.
     */
    val creator: JWTCreator.Builder = JWT
        .create()
        .withAudience(audience)
        .withIssuer(issuer)

    companion object {
        /**
         * The attribute key that allows the user to access the [JwtConfiguration] object within an application.
         */
        val KEY = AttributeKey<JwtConfiguration>("JwtConfiguration")
    }
}

/**
 * Extension method for accessing the [JwtConfiguration] instance of the [AuthenticationContext].
 */
val AuthenticationContext.jwt: JwtConfiguration get() = call.application.attributes[JwtConfiguration.KEY]
