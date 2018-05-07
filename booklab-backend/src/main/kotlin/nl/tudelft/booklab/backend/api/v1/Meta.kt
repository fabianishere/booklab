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

package nl.tudelft.booklab.backend.api.v1

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import nl.tudelft.booklab.backend.jwt
import java.time.Duration
import java.time.Instant
import java.util.Date

/**
 * Define meta endpoints at the current route for the REST api.
 */
fun Route.meta() {
    route("/auth") { auth() }
    get("/health") { call.respond(HealthCheck(true)) }
}

/**
 * Define the authentication endpoints at the current route of the REST api.
 */
private fun Route.auth() {
    // A development endpoint for generating JWT tokens.
    authenticate("passthrough") {
        post("/basic") {
            val user = call.authentication.principal<UserIdPrincipal>()

            if (user != null) {
                val validity = call.authentication.jwt.duration
                val token = call.authentication.jwt.run {
                    val now = Instant.now()

                    creator
                        .withIssuedAt(Date.from(now))
                        .withExpiresAt(Date.from(Instant.now().plus(duration)))
                        .withClaim("user", user.name)
                        .sign(algorithm)
                }
                // The validity of the token is represented in seconds
                call.respond(AuthenticationSuccessful(token, validity))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

/**
 * This class is returned when the authentication was successful.
 */
data class AuthenticationSuccessful(
    val token: String,

    @JsonProperty("expires_in")
    val expiresIn: Duration
)

/**
 * This class represents the result of a health check.
 */
data class HealthCheck(val success: Boolean)
