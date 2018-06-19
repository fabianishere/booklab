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

import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import nl.tudelft.booklab.backend.booklab
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.support.beans

/**
 * Unit test suite for the authentication endpoints of the BookLab REST api.
 *
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
internal class AuthTest {
    @Test
    fun `authorization via password is allowed`() = withTestEngine({ module() }) {
        val parameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "test")
            append("client_secret", "test")
            append("scopes", "test")
            append("username", "fabianishere@outlook.com")
            append("password", "test")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `authorization via client credentials is allowed`() = withTestEngine({ module() }) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("client_id", "test")
            append("client_secret", "test")
            append("scopes", "test")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean("routes") { Routes.from { routes() } }
            }.initialize(this)
        }
        context.bootstrap(this) { booklab() }
    }

    /**
     * The routes of the application.
     */
    private fun Routing.routes() {
        route("/api") { meta() }
    }
}
