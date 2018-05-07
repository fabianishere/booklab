/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.booklab.backend.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import nl.tudelft.booklab.backend.JwtConfiguration
import nl.tudelft.booklab.backend.withTestEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date

/**
 * Unit test suite for the detection endpoint of the BookLab REST api.
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
internal class DetectionTest {
    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
    }

    @Test
    fun `put returns proper interface`() = withTestEngine {
        val request = handleRequest(HttpMethod.Put, "/api/detection") {
            val token = application.attributes[JwtConfiguration.KEY].run {
                val now = Instant.now()

                creator
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(Instant.now().plus(duration)))
                    .withClaim("user", "test@example.com")
                    .sign(algorithm)
            }
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: DetectionResult? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
        }
    }

    @Test
    fun `put requires authentication`() = withTestEngine {
        val request = handleRequest(HttpMethod.Put, "/api/detection")
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }
}
