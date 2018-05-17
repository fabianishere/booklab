/*
 * Copyright 2018 Fabian Mastenbroek.
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
import io.ktor.auth.oauth2.grant.ClientCredentialsGrantHandler
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.formUrlEncode
import io.ktor.http.fullPath
import io.ktor.http.takeFrom
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.encodeBase64
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test suite for the token endpoint of the [OAuthServer] class.
 *
 * The token endpoint is used by the client to obtain an access token by presenting its authorization grant or refresh
 * token. The token endpoint is used with every authorization grant except for the implicit grant type (since an access
 * token is issued directly).
 *
 * The means through which the client obtains the location of the token endpoint are beyond the scope of this
 * specification, but the location is typically provided in the service documentation.
 */
internal class TokenEndpointTest {
    private lateinit var server: OAuthServer<ClientIdPrincipal, UserIdPrincipal>
    private val client = "test"
    private val secret = "test"

    @BeforeEach
    fun setUp() {
        server = OAuthServer(
            handlers = mapOf("client_credentials" to ClientCredentialsGrantHandler()),
            clientRepository = TestClientRepository,
            tokenRepository = TestAccessTokenRepository
        )
    }

    /**
     * 3.2: The client MUST use the HTTP "POST" method when making access token requests.
     */
    @Test
    fun `get method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Get, "/api/auth/token")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.2: The client MUST use the HTTP "POST" method when making access token requests.
     */
    @Test
    fun `put method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Put, "/api/auth/token")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.2: The client MUST use the HTTP "POST" method when making access token requests.
     */
    @Test
    fun `head method not allowed`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Head, "/api/auth/token")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    /**
     * 3.2: Parameters sent without a value MUST be treated as if they were omitted from the request.
     * The authorization server MUST ignore unrecognized request parameters.  Request and response parameters
     * MUST NOT be included more than once.
     */
    @Test
    fun `parameters without value are treated as if they were omitted`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("invalid_request", content["error"])
            assertEquals("The 'grant_type' field is mandatory.", content["error_description"])
        }
    }

    @Test
    fun `grant type required`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Post, "/api/auth/token")
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("invalid_request", content["error"])
        }
    }

    @Test
    fun `unsupported grant type`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "test")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("unsupported_grant_type", content["error"])
        }
    }

    @Test
    fun `no redirect uri`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("invalid_request", content["error"])
        }
    }

    @Test
    fun `no client credentials`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("redirect_uri", "http://localhost:8080")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("invalid_request", content["error"])
        }
    }

    @Test
    fun `invalid client credentials`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("redirect_uri", "http://localhost:8080")
            append("client_id", client)
            append("client_secret", "123")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("invalid_client", content["error"])
        }
    }

    @Test
    fun `invalid client credentials using HTTP Basic`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("redirect_uri", "http://localhost:8080")
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
            val up = "$client:123"
            val encoded = encodeBase64(up.toByteArray(Charsets.ISO_8859_1))
            addHeader(HttpHeaders.Authorization, "Basic $encoded")
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("invalid_client", content["error"])
        }
    }

    @Test
    fun `no client secret allowed`() = withTestApplication(buildApplication(server)) {
        val parameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("redirect_uri", "http://localhost:8080")
            append("client_id", client)
        }
        val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
            setBody(parameters.formUrlEncode())
            addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
        }
        with(request) {
            val content = JSONValue.parse(response.content!!) as JSONObject
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("invalid_client", content["error"])
        }
    }

    /**
     * 3.2: The endpoint URI MAY include an "application/x-www-form-urlencoded" formatted (per Appendix B) query
     * component (https://tools.ietf.org/html/rfc6749#section-3.4), which MUST be retained when adding additional query
     * parameters. The endpoint URI MUST NOT include a fragment component.
     */
    @Test
    fun `receive valid token using query string`() = withTestApplication(buildApplication(server)) {
        val token = let {
            val url = URLBuilder().run {
                takeFrom("/api/auth/token")
                parameters.apply {
                    append("grant_type", "client_credentials")
                    append("redirect_uri", "http://localhost:8080")
                    append("client_id", client)
                    append("client_secret", secret)
                    append("scope", "test-a")
                }
                build()
            }
            val request = handleRequest(HttpMethod.Post, url.fullPath)
            with(request) {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = JSONValue.parse(response.content!!) as JSONObject
                content["access_token"]
            }
        }

        val request = handleRequest(HttpMethod.Get, "/protected/a") {
            addHeader("Authorization", "Bearer $token")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `receive valid token`() = withTestApplication(buildApplication(server)) {
        val token = let {
            val parameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("redirect_uri", "http://localhost:8080")
                append("client_id", client)
                append("client_secret", secret)
                append("scope", "test-a")
            }
            val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
                setBody(parameters.formUrlEncode())
                addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
            }
            with(request) {
                val content = JSONValue.parse(response.content!!) as JSONObject
                assertEquals(HttpStatusCode.OK, response.status())
                content["access_token"]
            }
        }

        val request = handleRequest(HttpMethod.Get, "/protected/a") {
            addHeader("Authorization", "Bearer $token")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `receive valid token using HTTP Basic`() = withTestApplication(buildApplication(server)) {
        val token = let {
            val parameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("redirect_uri", "http://localhost:8080")
                append("scope", "test-a")
            }
            val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
                setBody(parameters.formUrlEncode())
                addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
                addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
                val up = "$client:$secret"
                val encoded = encodeBase64(up.toByteArray(Charsets.ISO_8859_1))
                addHeader(HttpHeaders.Authorization, "Basic $encoded")
            }
            with(request) {
                val content = JSONValue.parse(response.content!!) as JSONObject
                assertEquals(HttpStatusCode.OK, response.status())
                content["access_token"]
            }
        }

        val request = handleRequest(HttpMethod.Get, "/protected/a") {
            addHeader("Authorization", "Bearer $token")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `endpoint is protected`() = withTestApplication(buildApplication(server)) {
        val request = handleRequest(HttpMethod.Get, "/protected/a")
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `endpoint with other scope not accessible`() = withTestApplication(buildApplication(server)) {
        val token = let {
            val parameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("redirect_uri", "http://localhost:8080")
                append("scope", "test-a")
            }
            val request = handleRequest(HttpMethod.Post, "/api/auth/token") {
                setBody(parameters.formUrlEncode())
                addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
                addHeader("Content-Type", ContentType.Application.FormUrlEncoded.toString())
                val up = "$client:$secret"
                val encoded = encodeBase64(up.toByteArray(Charsets.ISO_8859_1))
                addHeader(HttpHeaders.Authorization, "Basic $encoded")
            }
            with(request) {
                val content = JSONValue.parse(response.content!!) as JSONObject
                assertEquals(HttpStatusCode.OK, response.status())
                content["access_token"]
            }
        }

        val request = handleRequest(HttpMethod.Get, "/protected/b") {
            addHeader("Authorization", "Bearer $token")
        }
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }
}
