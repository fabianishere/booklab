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

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth2.oauth
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.routing.routing
import nl.tudelft.booklab.backend.api.v1.api
import nl.tudelft.booklab.backend.services.auth.BooklabOAuthServer
import nl.tudelft.booklab.backend.spring.inject

/**
 * Configure the given Ktor [Application] as Booklab backend application.
 */
fun Application.booklab() {
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) { configureJackson() }
    install(Authentication) { configureOAuth(inject()) }

    // Allow the different hosts to connect to the REST API
    install(CORS) {
        anyHost()

        // Allow the Authorization header to be sent to REST endpoints
        header(HttpHeaders.Authorization)
        method(HttpMethod.Post)
    }

    routing(inject())
}

/**
 * This type represents the routes of a Ktor application.
 */
typealias ApplicationRoutes = Routing.() -> Unit

/**
 * The routes of the application.
 */
internal fun Routing.routes() {
    route("/api") { api() }
}

/**
 * Configure the Jackson support for the [ContentNegotiation] feature.
 */
internal fun ContentNegotiation.Configuration.configureJackson() {
    jackson {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        registerModule(JavaTimeModule())
    }
}

/**
 * Configure the OAuth authentication providers for an application.
 *
 * @param oauth The [BooklabOAuthServer] to use for the authentication provider.
 */
internal fun Authentication.Configuration.configureOAuth(server: BooklabOAuthServer) {
    // Create an unnamed authentication provider for protecting resources using
    // the OAuth authorization server
    oauth(server)
}
