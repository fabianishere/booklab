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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import io.ktor.application.Application
import io.ktor.auth.authenticate
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import nl.tudelft.booklab.backend.booklab
import nl.tudelft.booklab.backend.configureAuthorization
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.services.auth.PersistentUserRepository
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.services.collection.BookCollection
import nl.tudelft.booklab.backend.services.collection.BookCollectionConversionService
import nl.tudelft.booklab.backend.services.collection.BookCollectionService
import nl.tudelft.booklab.backend.services.collection.BookCollectionServiceException
import nl.tudelft.booklab.backend.services.password.BCryptPasswordService
import nl.tudelft.booklab.backend.services.user.JacksonUserDeserializer
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.beans
import kotlin.test.assertNotNull

/**
 * Unit test suite for the users endpoint of the BookLab REST api.
 *
 * @author Fabian Mastenbroek (f.s.mastenbroek@student.tudelft.nl)
 */
internal class CollectionTest {
    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    lateinit var mapper: ObjectMapper

    /**
     * The mocked [BookCollectionService] class.
     */
    lateinit var collectionService: BookCollectionService

    /**
     * The mocked [UserService] class.
     */
    lateinit var userService: UserService

    /**
     * The mocked [CatalogueService] class.
     */
    lateinit var catalogueService: CatalogueService

    /**
     * Dummy books
     */
    val book1 = Book(
        id = "test",
        identifiers = mapOf(Identifier.INTERNAL to "test"),
        title = "The ontdekking van de hemel",
        authors = listOf("Harry Mulisch")
    )

    val book2 = Book(
        id = "hello",
        identifiers = mapOf(Identifier.INTERNAL to "hello"),
        title = "Steve Jobs",
        authors = listOf("Bill Gates")
    )

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
        collectionService = mock()
        userService = mock()
        catalogueService = mock()

        mapper.registerModule(SimpleModule().apply {
            addDeserializer(User::class.java, JacksonUserDeserializer(userService))
        })
    }

    @Test
    fun `client should be authorized`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections/1")
        with(request) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }

    @Test
    fun `client should have correct scope`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections/1") {
            configureAuthorization("test", listOf("bla"))
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
        }
    }

    @Test
    fun `invalid identifier`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections/test") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `collection not found`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections/2") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `method not allowed on collection`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Put, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("method_not_allowed", body?.error?.code)
        }
    }

    @Test
    fun `method not allowed on resource`() = withTestEngine({ module() }) {
        collectionService.stub {
            on { findById(eq(1)) } doReturn (BookCollection(1, null, "test", emptySet()))
            on { existsById(eq(1)) } doReturn (true)
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("method_not_allowed", body?.error?.code)
        }
    }

    @Test
    fun `resource retrieval success`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        collectionService.stub {
            on { findById(eq(1)) } doReturn BookCollection(1, user, "test", emptySet())
            on { existsById(eq(1)) } doReturn true
        }

        val request = handleRequest(HttpMethod.Get, "/api/collections/1") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<BookCollection>? = response.content?.let { mapper.readValue(it) }
            assertEquals(1, body?.data?.id)
        }
    }

    @Test
    fun `resource books retrieval success`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        val request = handleRequest(HttpMethod.Get, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(1, body?.data?.size)
        }
    }

    @Test
    fun `resource books add invalid credentials`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{}""")
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("forbidden", body?.error?.code)
        }
    }

    @Test
    fun `resource books add invalid structure`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{}""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
        }
    }

    @Test
    fun `resource books add invalid book`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
        }
    }

    @Test
    fun `resource books post success`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book2)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
            on { addBooks(any(), any()) } doAnswer { call ->
                val subject = call.getArgument<BookCollection>(0)
                val books = call.getArgument<Set<Book>>(1)
                subject.copy(books = subject.books + books)
            }
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<BookCollection>? = response.content?.let { mapper.readValue(it) }
            assertEquals(2, body?.data?.books?.size)
        }
    }

    @Test
    fun `resource books post constraint violation`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
            on { addBooks(any(), any()) } doThrow BookCollectionServiceException.InvalidInformationException("This is staged")
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
            assertEquals("This is staged", body?.error?.detail)
        }
    }

    @Test
    fun `resource books post unexpected exception`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
            on { addBooks(any(), any()) } doAnswer {
                throw Exception("This is staged")
            }
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("server_error", body?.error?.code)
        }
    }

    @Test
    fun `resource books put success`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1, book2)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
            on { setBooks(any(), any()) } doAnswer { call ->
                val subject = call.getArgument<BookCollection>(0)
                val books = call.getArgument<Set<Book>>(1)
                subject.copy(books = books)
            }
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Put, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<BookCollection>? = response.content?.let { mapper.readValue(it) }
            assertEquals(1, body?.data?.books?.size)
        }
    }

    @Test
    fun `resource books delete success`() = withTestEngine({ module() }) {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1, book2)
        )
        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
            on { deleteBooks(any(), any()) } doAnswer { call ->
                val subject = call.getArgument<BookCollection>(0)
                val books = call.getArgument<Set<Book>>(1)
                subject.copy(books = subject.books - books)
            }
        }

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
            on { findById(eq(1)) } doReturn user
            on { existsById((eq(1))) } doReturn true
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Delete, "/api/collections/1/books") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<BookCollection>? = response.content?.let { mapper.readValue(it) }
            assertEquals(1, body?.data?.books?.size)
        }
    }

    @Test
    fun `create collection without valid user`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("forbidden", body?.error?.code)
        }
    }

    @Test
    fun `create collection with invalid request`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }
        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{}""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
        }
    }

    @Test
    fun `create collection with invalid book`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
            assertEquals("The collection 'test' could not be found in the catalogue.", body?.error?.detail)
        }
    }

    @Test
    fun `create duplicate collection`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }

        collectionService.stub {
            on { save(any()) } doThrow BookCollectionServiceException.BookCollectionAlreadyExistsException("")
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.Conflict, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("resource_exists", body?.error?.code)
        }
    }

    @Test
    fun `create invalid collection`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }

        collectionService.stub {
            on { save(any()) } doThrow BookCollectionServiceException.InvalidInformationException("")
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("invalid_request", body?.error?.code)
        }
    }

    @Test
    fun `create collection unexpected error`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }

        collectionService.stub {
            on { save(any()) } doThrow RuntimeException("This is staged")
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("server_error", body?.error?.code)
        }
    }

    @Test
    fun `create collection success`() = withTestEngine({ module() }) {
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn User(1, "test@example.com", "")
        }

        collectionService.stub {
            on { save(any()) } doAnswer { it.getArgument(0) }
        }

        catalogueService.stub {
            onBlocking { findById(eq("test")) } doReturn book1
        }

        val request = handleRequest(HttpMethod.Post, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
            setBody("""{ "name" : "test", "books" : ["test"] }""")
        }
        with(request) {
            assertEquals(HttpStatusCode.Created, response.status())
            val body: ApiResponse.Success<BookCollection>? = response.content?.let { mapper.readValue(it) }
            assertEquals("test", body?.data?.name)
        }
    }

    @Test
    fun `find collection by non-present user id`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `find collection by invalid user id`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections?user=test") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `find collection by unknown user id`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/collections?user=1") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.NotFound, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("not_found", body?.error?.code)
        }
    }

    @Test
    fun `find collection by user id`() = withTestEngine({ module() }) {
        val collections = mutableSetOf<BookCollection>()
        val user = User(1, "test@example.com", "", collections)
        collections += BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )

        userService.stub {
            on { findById(eq(1)) } doReturn user
        }

        val request = handleRequest(HttpMethod.Get, "/api/collections?user=1") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<Set<BookCollection>>? = response.content?.let { mapper.readValue(it) }
            assertEquals(collections, body?.data)
        }
    }

    @Test
    fun `delete collection with invalid user`() = withTestEngine({ module() }) {
        val collections = mutableSetOf<BookCollection>()
        val user = User(1, "test@example.com", "", collections)
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collections += collection

        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        val request = handleRequest(HttpMethod.Delete, "/api/collections/1/") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.Forbidden, response.status())
            val body: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertEquals("forbidden", body?.error?.code)
        }
    }

    @Test
    fun `delete collection with valid user`() = withTestEngine({ module() }) {
        val collections = mutableSetOf<BookCollection>()
        val user = User(1, "test@example.com", "", collections)
        val collection = BookCollection(
            id = 1,
            user = user,
            name = "test",
            books = setOf(book1)
        )
        collections += collection

        collectionService.stub {
            on { findById(eq(1)) } doReturn collection
            on { existsById(eq(1)) } doReturn true
        }

        userService.stub {
            on { findById(eq(1)) } doReturn user
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        val request = handleRequest(HttpMethod.Delete, "/api/collections/1/") {
            configureAuthorization("test", listOf("collection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val body: ApiResponse.Success<Unit>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(body)
            verify(collectionService).delete(eq(collection))
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean("routes") { Routes.from { routes() } }

                bean("oauth:repository:user") { PersistentUserRepository(ref(), ref()) }

                // UserService
                bean { userService }
                bean { catalogueService }
                bean { collectionService }

                bean("user:jackson-deserializer") { JacksonUserDeserializer(ref()) }
                bean("collection:conversion-service") { BookCollectionConversionService(ref()) }

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
            route("/api/collections") {
                collections()
            }
        }
    }
}
