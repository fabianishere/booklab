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

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.JwtAccessTokenRepository
import io.ktor.auth.oauth2.repository.JwtConfiguration
import io.ktor.auth.oauth2.repository.UserHashedTableRepository
import io.ktor.auth.oauth2.util.toJson
import io.ktor.auth.oauth2.util.toRedirectUri
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.httpMethod
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import java.time.Duration

/**
 * A simple application setup used for testing.
 */
fun buildApplication(
    server: OAuthServer<ClientIdPrincipal, UserIdPrincipal>
): Application.() -> Unit = {
    install(Authentication) {
        oauth<ClientIdPrincipal, UserIdPrincipal> {
            this.server = server
            scopes = setOf("test-a")
        }

        oauth<ClientIdPrincipal, UserIdPrincipal>("scopes:test") {
            this.server = server
            scopes = setOf("test-b")
        }
    }

    routing {
        route("/api/auth") {
            route("/token") {
                oauthTokenEndpoint(server)
            }

            route("/authorize") {
                handle {
                    if (call.request.httpMethod !in setOf(HttpMethod.Get, HttpMethod.Post)) {
                        call.respond(HttpStatusCode.MethodNotAllowed)
                        return@handle
                    }

                    val state = call.parameters.state
                    val baseUri = try {
                        call.parameters.redirectUri() ?: throw InvalidRequest("The 'redirect_uri' field is mandatory.")
                    } catch (e: InvalidRequest) {
                        call.respondText(e.toJson(state), ContentType.Application.Json, e.status)
                        return@handle
                    }

                    val redirectUri = try {
                        val request = call.getAuthorizationRequest(server)
                        val authorization = request.authorize(UserIdPrincipal("test@example.com"))
                        authorization.toRedirectUri(request.redirectUri)
                    } catch (e: OAuthError) {
                        application.log.debug("The authorization request failed", e)
                        e.toRedirectUri(baseUri, state)
                    }

                    call.respondRedirect(redirectUri)
                }
            }
        }

        authenticate {
            get("/protected/a") {
                call.respond(HttpStatusCode.OK)
            }
        }

        authenticate("scopes:test") {
            get("/protected/b") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

val TestClientRepository = ClientHashedTableRepository(
    digester = { it.toByteArray() },
    table = mapOf("test" to (ClientIdPrincipal("test", scopes = setOf("test-a", "test-b")) to "test".toByteArray()))
)

val TestUserRepository = UserHashedTableRepository(
    digester = { it.toByteArray() },
    table = mapOf("test" to "test".toByteArray())
)

val TestAccessTokenRepository = JwtAccessTokenRepository(
    configuration = JwtConfiguration("test", "test", Algorithm.HMAC512("test")),
    userRepository = TestUserRepository,
    clientRepository = TestClientRepository,
    validity = Duration.ofMinutes(15)
)
