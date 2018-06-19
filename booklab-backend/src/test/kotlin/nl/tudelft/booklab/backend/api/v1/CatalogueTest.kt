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
import com.nhaarman.mockitokotlin2.verifyBlocking
import io.ktor.application.Application
import io.ktor.auth.authenticate
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import nl.tudelft.booklab.backend.booklab
import nl.tudelft.booklab.backend.configureAuthorization
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueConversionService
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.beans
import nl.tudelft.booklab.catalogue.Book as AbstractBook

/**
 * Unit test suite for the catalogue endpoint of the BookLab REST api.
 */
internal class CatalogueTest {
    /**
     * The [CatalogueService] to use.
     */
    lateinit var catalogue: CatalogueService

    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
        mapper.registerModule(SimpleModule().apply {
            addAbstractTypeMapping(AbstractBook::class.java, Book::class.java)
        })
        catalogue = mock()
    }

    @Test
    fun `client should be authorized`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/catalogue")
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `client should have correct scope`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/catalogue") {
            configureAuthorization("test", listOf("test"))
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
        }
    }

    @Test
    fun `query catalogue without parameters`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/catalogue") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `query catalogue with keywords`() = withTestEngine({ module() }) {
        val books = listOf(Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        ))

        catalogue.stub {
            onBlocking { query(eq("hello"), eq(5)) } doReturn books
        }
        val request = handleRequest(HttpMethod.Get, "/api/catalogue?query=hello") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(books, response?.data)
        }

        verifyBlocking(catalogue) {
            query(eq("hello"), eq(5))
        }
    }

    @Test
    fun `query catalogue with title and author`() = withTestEngine({ module() }) {
        val books = listOf(Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        ))
        catalogue.stub {
            onBlocking { query(eq("Test"), eq("Test"), eq(5)) } doReturn books
        }
        val request = handleRequest(HttpMethod.Get, "/api/catalogue?title=Test&author=Test") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(books, response?.data)
        }

        verifyBlocking(catalogue) {
            query(eq("Test"), eq("Test"), eq(5))
        }
    }

    @Test
    fun `query catalogue with title but not author`() = withTestEngine({ module() }) {
        val books = listOf(Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        ))
        catalogue.stub {
            onBlocking { query(eq("Test"), eq("Test"), eq(5)) } doReturn books
        }
        val request = handleRequest(HttpMethod.Get, "/api/catalogue?title=Test") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `query catalogue with title and author with custom limit`() = withTestEngine({ module() }) {
        val books = listOf(Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        ))
        catalogue.stub {
            onBlocking { query(eq("Test"), eq("Test"), eq(10)) } doReturn books
        }
        val request = handleRequest(HttpMethod.Get, "/api/catalogue?title=Test&author=Test&max=10") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(books, response?.data)
        }

        verifyBlocking(catalogue) {
            query(eq("Test"), eq("Test"), eq(10))
        }
    }

    @Test
    fun `query catalogue with title and author with invalid limit`() = withTestEngine({ module() }) {
        val books = listOf(
            Book(
                id = "test",
                identifiers = mapOf(Identifier.INTERNAL to "test"),
                title = "The ontdekking van de hemel",
                authors = listOf("Harry Mulisch")
            )
        )
        catalogue.stub {
            onBlocking { query(eq("Test"), eq("Test"), eq(5)) } doReturn books
        }
        val request = handleRequest(HttpMethod.Get, "/api/catalogue?title=Test&author=Test&max=twenty") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(books, response?.data)
        }

        verifyBlocking(catalogue) {
            query(eq("Test"), eq("Test"), eq(5))
        }
    }

    @Test
    fun `query with method other than GET`() = withTestEngine({ module() }) {

        val request = handleRequest(HttpMethod.Post, "/api/catalogue?title=Test&author=Test&max=10") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("method_not_allowed", response?.error?.code)
        }
    }

    @Test
    fun `lookup unknown book`() = withTestEngine({ module() }) {

        val request = handleRequest(HttpMethod.Get, "/api/catalogue/1") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", response?.error?.code)
        }
    }

    @Test
    fun `lookup known book`() = withTestEngine({ module() }) {
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )
        catalogue.stub {
            onBlocking { findById(eq("1")) } doReturn book
        }

        val request = handleRequest(HttpMethod.Get, "/api/catalogue/1") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Success<Book>? = response.content?.let { mapper.readValue(it) }
            assertEquals(book, response?.data)
        }
    }

    @Test
    fun `lookup book with invalid method`() = withTestEngine({ module() }) {

        val request = handleRequest(HttpMethod.Post, "/api/catalogue/1") {
            configureAuthorization("test", listOf("catalogue"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            assertTrue(response.contentType().match(ContentType.Application.Json))

            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("method_not_allowed", response?.error?.code)
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean("routes") { Routes.from { routes() } }

                bean { catalogue }

                bean { CatalogueConversionService(ref()) }
            }.initialize(this)
        }
        context.bootstrap(this) { booklab() }
    }

    /**
     * The routes of the application.
     */
    private fun Routing.routes() {
        authenticate {
            route("/api/catalogue") {
                catalogue()
            }
        }
    }
}
