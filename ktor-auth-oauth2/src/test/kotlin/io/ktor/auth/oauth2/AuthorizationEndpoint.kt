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

import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.oauth2.grant.AuthorizationCodeGrantHandler
import io.ktor.auth.oauth2.grant.ClientCredentialsGrantHandler
import io.ktor.auth.oauth2.grant.ImplicitGrantHandler
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.MemoryAuthorizationCodeRepository
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.fullPath
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.http.takeFrom
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI

/**
 * Test suite for the token endpoint of the [OAuthServer] class.
 *
 * The authorization endpoint is used to interact with the resource owner and obtain an authorization grant.
 * The authorization server MUST first verify the identity of the resource owner.  The way in which the authorization
 * server authenticates the resource owner (e.g., username and password login, session cookies) is beyond the
 * scopes of this specification.
 *
 * The means through which the client obtains the location of the authorization endpoint are beyond the scopes of this
 * specification, but the location is typically provided in the service documentation.
 */
internal class AuthorizationEndpointTest {
    private lateinit var server: OAuthServer<ClientIdPrincipal, UserIdPrincipal>

    @BeforeEach
    fun setUp() {
        server = OAuthServer(
            handlers = mapOf(
                "authorization_code" to AuthorizationCodeGrantHandler(MemoryAuthorizationCodeRepository()),
                "implicit" to ImplicitGrantHandler(),
                "client_credentials" to ClientCredentialsGrantHandler()
            ),
            clientRepository = TestClientRepository,
            tokenRepository = TestAccessTokenRepository
        )
    }

    /**
     * 3.1: The authorization server MUST support the use of the HTTP "GET" method for the authorization
     * endpoint and MAY support the use of the "POST" method as well.
     */
    @Test
    fun `delete method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Delete, "/api/auth/authorize")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.1: The authorization server MUST support the use of the HTTP "GET" method for the authorization
     * endpoint and MAY support the use of the "POST" method as well.
     */
    @Test
    fun `put method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Put, "/api/auth/authorize")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.1: The authorization server MUST support the use of the HTTP "GET" method for the authorization
     * endpoint and MAY support the use of the "POST" method as well.
     */
    @Test
    fun `head method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Head, "/api/auth/authorize")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.1: Parameters sent without a value MUST be treated as if they were omitted from the request.
     * The authorization server MUST ignore unrecognized request parameters.  Request and response parameters
     * MUST NOT be included more than once.
     */
    @Test
    fun `parameters without value are treated as if they were omitted`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "")
                append("redirect_uri", "http://localhost")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertEquals("invalid_request", content["error"])
            assertEquals("The 'response_type' field is mandatory.", content["error_description"])
        }
    }

    @Test
    fun `response type required`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("redirect_uri", "http://localhost")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertEquals("invalid_request", content["error"])
            assertEquals("The 'response_type' field is mandatory.", content["error_description"])
        }
    }

    @Test
    fun `unsupported response type`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "client_credentials")
                append("redirect_uri", "http://localhost")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertEquals("unsupported_response_type", content["error"])
        }
    }

    @Test
    fun `no redirect uri`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "token")
                append("client_id", "test")
                append("client_secret", "test")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals("invalid_request", content["error"])
            assertEquals("The 'redirect_uri' field is mandatory.", content["error_description"])
        }
    }

    @Test
    fun `invalid redirect uri`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "token")
                append("client_id", "test")
                append("client_secret", "test")
                append("redirect_uri", "\\")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("invalid_request", content["error"])
            assertEquals("The given request uri is invalid.", content["error_description"])
        }
    }

    @Test
    fun `no client credentials`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "token")
                append("redirect_uri", "http://localhost")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertEquals("invalid_request", content["error"])
            assertEquals("The client credentials are required.", content["error_description"])
        }
    }

    @Test
    fun `invalid client credentials`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "token")
                append("client_id", "bla")
                append("redirect_uri", "http://localhost")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertEquals("invalid_client", content["error"])
        }
    }

    @Test
    fun `client secret ignored`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "implicit")
                append("client_id", "test")
                append("client_secret", "bla")
                append("redirect_uri", "http://localhost:8080")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
        }
    }

    @Test
    fun `redirect to correct url`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "code")
                append("client_id", "test")
                append("client_secret", "test")
                append("scopes", "test-a")
                append("redirect_uri", "http://localhost:8080")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val uri = URI.create(response.headers[HttpHeaders.Location]!!)
            assertEquals(uri.host, "localhost")
        }
    }

    @Test
    fun `redirect uri contains state`() = withTestApplication(buildApplication(server)) {
        val state = "ten"
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "code")
                append("client_id", "test")
                append("client_secret", "test")
                append("scopes", "test-a")
                append("state", state)
                append("redirect_uri", "http://localhost:8080")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val uri = URI.create(response.headers[HttpHeaders.Location]!!)
            val content = uri.query.parseUrlEncodedParameters()
            assertEquals("localhost", uri.host)
            assertEquals(state, content["state"])
        }
    }

    @Test
    fun `get valid authorization code`() = withTestApplication(buildApplication(server)) {
        val url = URLBuilder().run {
            takeFrom("/api/auth/authorize")
            parameters.apply {
                append("response_type", "code")
                append("client_id", "test")
                append("client_secret", "test")
                append("scopes", "test-a")
                append("redirect_uri", "http://localhost:8080")
            }
            build()
        }
        val request = handleRequest(HttpMethod.Post, url.fullPath)
        with(request) {
            assertEquals(HttpStatusCode.Found, response.status())
            val content = URI.create(response.headers[HttpHeaders.Location]!!).query.parseUrlEncodedParameters()
            assertNotNull(content["code"])
        }
    }
}
