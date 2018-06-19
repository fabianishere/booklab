package nl.tudelft.booklab.backend.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import nl.tudelft.booklab.backend.booklab
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.services.collection.BookCollection
import nl.tudelft.booklab.backend.services.collection.BookCollectionService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.recommender.Recommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.beans
import nl.tudelft.booklab.catalogue.Book as AbstractBook

/**
 * Test suite for the recommendation endpoint.
 */
internal class RecommendationTest {
    /**
     * The [ObjectMapper] to map the responses back to POJOs.
     */
    private lateinit var mapper: ObjectMapper

    /**
     * The recommender mock we use.
     */
    private lateinit var recommender: Recommender

    /**
     * The [BookCollectionService] to use.
     */
    private lateinit var collections: BookCollectionService

    /**
     * The [CatalogueService] to use.
     */
    private lateinit var catalogue: CatalogueService

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
        mapper.registerModule(SimpleModule().apply {
            addAbstractTypeMapping(AbstractBook::class.java, Book::class.java)
        })
        recommender = mock()
        collections = mock()
        catalogue = mock()
    }

    @Test
    fun `post returns proper interface`() = withTestEngine({ module() }) {
        val collection = listOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "title 1", null, listOf("author 1"))
        )

        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        recommender.stub {
            onBlocking { recommend(any(), any()) } doReturn candidates
        }

        catalogue.stub {
            onBlocking { findById(eq("1")) } doReturn collection[0]
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(collection.map { it.id }, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(candidates, response?.data)
        }
    }

    @Test
    fun `post lookup unknown candidate`() = withTestEngine({ module() }) {
        val collection = listOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "title 1", null, listOf("author 1"))
        )

        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        recommender.stub {
            onBlocking { recommend(any(), any()) } doReturn candidates
        }

        catalogue.stub {
            onBlocking { findById(eq("1")) } doReturn collection[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(collection.map { it.id }, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `post lookup unknown collection`() = withTestEngine({ module() }) {
        val collection = listOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "title 1", null, listOf("author 1"))
        )

        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        recommender.stub {
            onBlocking { recommend(any(), any()) } doReturn candidates
        }

        catalogue.stub {
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(collection.map { it.id }, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `post lookup collection of user`() = withTestEngine({ module() }) {
        val collection = BookCollection(1, null, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "title 1", null, listOf("author 1")),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "title 2", null, listOf("author 2")),
            Book("3", mapOf(Identifier.INTERNAL to "3"), "title 3", null, listOf("author 2")),
            Book("4", mapOf(Identifier.INTERNAL to "4"), "title 4", null, listOf("author 2")),
            Book("5", mapOf(Identifier.INTERNAL to "5"), "title 5", null, listOf("author 3")),
            Book("6", mapOf(Identifier.INTERNAL to "6"), "title 6", null, listOf("author 3"))
        ))
        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        recommender.stub {
            onBlocking { recommend(any(), any()) } doReturn candidates
        }

        collections.stub {
            on { findById(eq(1)) } doReturn collection
        }

        catalogue.stub {
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(null, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations?collection=1") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: ApiResponse.Success<List<Book>>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(candidates, response?.data)
        }
    }

    @Test
    fun `post lookup invalid collection of user`() = withTestEngine({ module() }) {
        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        recommender.stub {
            onBlocking { recommend(any(), any()) } doReturn candidates
        }

        catalogue.stub {
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(null, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations?collection=test") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `post lookup unknown collection of user`() = withTestEngine({ module() }) {
        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        catalogue.stub {
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(null, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations?collection=2") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `post fails on partial input`() = withTestEngine({ module() }) {
        val candidates = listOf(
            Book("7", mapOf(Identifier.INTERNAL to "7"), "title 7", null, listOf("author 1")),
            Book("8", mapOf(Identifier.INTERNAL to "8"), "title 8", null, listOf("author 2")),
            Book("9", mapOf(Identifier.INTERNAL to "9"), "title 9", null, listOf("author 3"))
        )

        catalogue.stub {
            onBlocking { findById(eq("7")) } doReturn candidates[0]
            onBlocking { findById(eq("8")) } doReturn candidates[1]
            onBlocking { findById(eq("9")) } doReturn candidates[2]
        }

        val input = RecommendationRequest(null, candidates.map { it.id })
        val body = mapper.writeValueAsString(input)

        val request = handleRequest(HttpMethod.Post, "/api/recommendations") {
            setBody(body)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `post fails on invalid input`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Post, "/api/recommendations") {
            setBody("{}")
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("invalid_request", response?.error?.code)
        }
    }

    @Test
    fun `non-POST calls are not allowed`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Put, "/api/recommendations") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("method_not_allowed", response?.error?.code)
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean("routes") { Routes.from { routes() } }

                bean { recommender }
                bean { collections }
                bean { catalogue }
            }.initialize(this)
        }
        context.bootstrap(this) { booklab() }
    }

    /**
     * The routes of the application.
     */
    private fun Routing.routes() {
        route("/api/recommendations") { recommendation() }
    }
}
