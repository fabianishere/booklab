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
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.application.Application
import io.ktor.auth.authenticate
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.testing.handleRequest
import nl.tudelft.booklab.backend.booklab
import nl.tudelft.booklab.backend.configureAuthorization
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.services.auth.PersistentUserRepository
import nl.tudelft.booklab.backend.services.password.BCryptPasswordService
import nl.tudelft.booklab.backend.services.user.JacksonUserDeserializer
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserConversionService
import nl.tudelft.booklab.backend.services.user.UserService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.beans

/**
 * Unit test suite for the users endpoint of the BookLab REST api.
 *
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
internal class UserProfileTest {
    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    lateinit var mapper: ObjectMapper

    /**
     * The mocked [UserService] class.
     */
    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
        userService = mock()
        mapper.registerModule(SimpleModule().apply {
            addDeserializer(User::class.java, JacksonUserDeserializer(userService))
        })
    }

    @Test
    fun `client should be authorized`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/users/1")
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `client should have correct scope`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/users/1") {
            configureAuthorization("test", listOf("detection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
        }
    }

    @Test
    fun `invalid identifier`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/users/test") {
            configureAuthorization("test", listOf("user:profile"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `user not found`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/users/2") {
            configureAuthorization("test", listOf("user:profile"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `method not allowed`() = withTestEngine({ module() }) {
        userService.stub {
            on { findById(eq(1)) } doReturn (User(1, "test@example.com", ""))
            on { existsById(eq(1)) } doReturn (true)
        }

        val request = handleRequest(HttpMethod.Post, "/api/users/1") {
            configureAuthorization("test", listOf("user:profile"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("method_not_allowed", body?.error?.code)
        }
    }

    @Test
    fun success() = withTestEngine({ module() }) {
        userService.stub {
            on { findById(eq(1)) } doReturn (User(1, "test@example.com", ""))
            on { existsById(eq(1)) } doReturn (true)
        }
        val request = handleRequest(HttpMethod.Get, "/api/users/1") {
            configureAuthorization("test", listOf("user:profile"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<User>? = response.content?.let { mapper.readValue(it) }
            assertEquals(1, body?.data?.id)
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean("routes") { Routes.from { routes() } }

                bean("oauth:repository:user") { PersistentUserRepository(ref(), ref()) }

                // UserService
                bean("user:user-service") { userService }
                bean("user:conversion-service") { UserConversionService(ref()) }

                // PasswordService
                bean { BCryptPasswordService() }
            }.initialize(this)
        }

        context.bootstrap(this) { booklab() }
    }

    /**
     * The routes of the application.
     */
    private fun Routing.routes() {
        authenticate {
            route("/api/users") {
                users()
            }
        }
    }
}
