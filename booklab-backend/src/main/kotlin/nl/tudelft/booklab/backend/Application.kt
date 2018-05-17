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
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.oauth2.oauth
import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.UserHashedTableRepository
import io.ktor.auth.oauth2.repository.parseClients
import io.ktor.auth.oauth2.repository.parseUsers
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.getDigestFunction
import nl.tudelft.booklab.backend.api.v1.api
import nl.tudelft.booklab.backend.auth.JwtConfiguration
import nl.tudelft.booklab.backend.auth.OAuthConfiguration
import nl.tudelft.booklab.backend.auth.buildJwtConfiguration

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
        // We create an OAuth authorization server for authorizing REST API calls
        val jwt = buildJwtConfiguration(environment.config.config("auth.jwt")).also {
            attributes.put(JwtConfiguration.KEY, it)
        }
        val clientRepository = ClientHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = environment.config.config("auth").parseClients()
        )
        val userRepository = UserHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = environment.config.config("auth").parseUsers()
        )
        val oauth = OAuthConfiguration(clientRepository, userRepository, jwt).also {
            attributes.put(OAuthConfiguration.KEY, it)
        }

        // Create an unnamed authentication provider for protecting resources using
        // the OAuth authorization server.
        oauth<ClientIdPrincipal, UserIdPrincipal>("rest:detection") {
            server = oauth.server
            scopes = setOf("detection")
        }
    }

    // Allow the different hosts to connect to the REST API
    install(CORS) {
        anyHost()

        // Allow the Authorization header to be sent to REST endpoints
        header(HttpHeaders.Authorization)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
    }

    routing {
        route("/api") {
            api()
        }
    }
}
