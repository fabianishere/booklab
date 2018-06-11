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

import io.ktor.application.call
import io.ktor.auth.oauth2.oauthTokenEndpoint
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.route
import nl.tudelft.booklab.backend.services.auth.BooklabOAuthServer
import nl.tudelft.booklab.backend.spring.inject

/**
 * Define meta endpoints at the current route for the REST api.
 */
fun Route.meta() {
    route("/auth") { auth() }
    get("/health") { call.respond(HealthCheck(true)) }
}

/**
 * Define the authentication endpoints at the current route of the REST api.
 *
 */
internal fun Route.auth() {
    val server: BooklabOAuthServer = application.inject()

    // Define OAuth token endpoint
    route("/token") { oauthTokenEndpoint(server) }

    // TODO define proper OAuth authorization endpoint
    // This is already supported by the ktor-auth-oauth2 package, but needs some implementation on our side.
}

/**
 * This class represents the result of a health check.
 */
data class HealthCheck(val success: Boolean)
