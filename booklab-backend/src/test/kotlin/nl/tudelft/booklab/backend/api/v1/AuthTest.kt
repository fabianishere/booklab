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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.encodeBase64
import nl.tudelft.booklab.backend.JwtConfiguration
import nl.tudelft.booklab.backend.withTestEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit test suite for the authentication endpoints of the BookLab REST api.
 *
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
internal class AuthTest {
    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
    }

    @Test
    fun `basic authentication should return valid token`() = withTestEngine {
        val verifier = application.attributes[JwtConfiguration.KEY].verifier
        with(handleRequestWithBasic(HttpMethod.Post, "/api/auth/basic", "test@example.com", "test")) {
            assertEquals(HttpStatusCode.OK, response.status())

            val result: AuthenticationSuccessful? = response.content?.let { mapper.readValue(it) }
            assertNotNull(result)
            val token = verifier.verify(result!!.token)
            assertNotNull(token)
            assertEquals(token?.getClaim("user")?.asString(), "test@example.com")
        }
    }

    @Test
    fun `basic authentication without credentials should fail`() = withTestEngine {
        with(handleRequest(HttpMethod.Post, "/api/auth/basic")) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    private fun TestApplicationEngine.handleRequestWithBasic(method: HttpMethod, url: String, user: String, pass: String) =
        handleRequest {
            this.method = method
            uri = url
            val up = "$user:$pass"
            val encoded = encodeBase64(up.toByteArray(Charsets.ISO_8859_1))
            addHeader(HttpHeaders.Authorization, "Basic $encoded")
        }
}
