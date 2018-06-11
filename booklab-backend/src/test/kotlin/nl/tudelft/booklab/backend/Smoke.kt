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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import nl.tudelft.booklab.backend.api.v1.HealthCheck
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit

/**
 * Smoke test for the BookLab backend server.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SmokeTest {
    /**
     * The test environment to use.
     */
    private lateinit var engine: TestApplicationEngine

    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    private lateinit var mapper: ObjectMapper

    /**
     * Set up the smoke test
     */
    @BeforeAll
    fun setupClass() {
        assumeTrue(System.getenv().containsKey("GOOGLE_BOOKS_API_KEY"))
        assumeTrue(System.getenv().containsKey("GOOGLE_APPLICATION_CREDENTIALS"))

        engine = TestApplicationEngine(createTestEnvironment(Application::bootstrap))
        engine.start()

        mapper = jacksonObjectMapper()
    }

    /**
     * Tear down the smoke test.
     */
    @AfterAll
    fun tearDownClass() {
        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }

    @Test
    fun `health check should return true`() = with(engine) {
        with(handleRequest(HttpMethod.Get, "/api/health")) {
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
            Assertions.assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: HealthCheck? = response.content?.let { mapper.readValue(it) }
            Assertions.assertEquals(HealthCheck(true), response)
        }
    }
}
