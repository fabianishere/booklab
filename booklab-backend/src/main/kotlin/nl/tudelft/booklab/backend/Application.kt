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

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import nl.tudelft.booklab.backend.api.v1.api
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * The main entry point of the BookLab web application.
 */
fun Application.booklab() {
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            registerModule(JavaTimeModule())
        }
    }
    install(Authentication) {
        // The JWT authentication method authenticates users for REST calls using tokens
        jwt {
            val config = let {
                val issuer = environment.config.property("jwt.domain").getString()
                val audience = environment.config.property("jwt.audience").getString()
                val realm = environment.config.property("jwt.realm").getString()
                val passphrase = environment.config.property("jwt.passphrase").getString()
                val duration = Duration.of(environment.config.property("jwt.duration").getString().toLong(),
                    ChronoUnit.MILLIS)

                JwtConfiguration(issuer, audience, realm, duration, Algorithm.HMAC512(passphrase))
            }.also { attributes.put(JwtConfiguration.KEY, it) }

            realm = config.realm
            verifier(config.verifier)
            validate { credential ->
                if (credential.payload.audience.contains(config.audience)) JWTPrincipal(credential.payload) else null
            }
        }

        // This authentication method is used for testing purposes and accepts all user-password combinations.
        basic("passthrough") {
            realm = "Booklab"
            validate { credentials -> UserIdPrincipal(credentials.name) }
        }
    }

    // Allow the different hosts to connect to the REST API
    install(CORS) {
        anyHost()
        method(HttpMethod.Put)
    }

    routing {
        route("/api") {
            api()
        }
    }
}
