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
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
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
import nl.tudelft.booklab.backend.configureAuthorization
import nl.tudelft.booklab.backend.createTestContext
import nl.tudelft.booklab.backend.services.vision.VisionService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.opencv.core.Rect
import org.springframework.context.support.beans
import java.net.URL
import nl.tudelft.booklab.catalogue.Book as AbstractBook

/**
 * A domain model class representing a book.
 *
 * @property id The internal identifier of the book in the database.
 * @property identifiers A map containing the identifiers of the book.
 * @property title The main title of the book.
 * @property subtitle The subtitle of the book.
 * @property authors A list of authors of the book.
 */
data class Book(
    val id: String,
    override val identifiers: Map<Identifier, String>,
    override val title: String,
    override val authors: List<String>
) : AbstractBook() {
    override val publisher: String? = null
    override val subtitle: String? = null
    override val categories = emptySet<String>()
    override val publishedAt = null
    override val description = null
    override val language = null
    override val images = emptyMap<String, URL>()
    override val ratings = null
}

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
    private lateinit var mapper: ObjectMapper

    /**
     * The collection detector to use.
     */
    private lateinit var detector: BookDetector

    /**
     * The text extractor to use.
     */
    private lateinit var extractor: TextExtractor

    /**
     * The catalogue client to use.
     */
    private lateinit var catalogue: CatalogueClient

    @BeforeEach
    fun setUp() {
        mapper = jacksonObjectMapper()
        mapper.registerModule(SimpleModule().apply {
            addAbstractTypeMapping(AbstractBook::class.java, Book::class.java)
        })
        detector = mock()
        extractor = mock()
        catalogue = mock()
    }

    @Test
    fun `post returns proper interface`() = withTestEngine({ module() }) {
        detector.stub {
            on { detect(any()) } doReturn(listOf(Rect(2, 3, 4, 5)))
        }
        extractor.stub {
            on { batch(any()) } doReturn (listOf("De ontdekking van Harry Mulisch"))
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn (listOf(
                Book(
                    id = "test",
                    identifiers = mapOf(Identifier.INTERNAL to "test"),
                    title = "The ontdekking van de hemel",
                    authors = listOf("Harry Mulisch")
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: DetectionResult? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.size)
        }
    }

    @Test
    fun `post handles invalid estimation`() = withTestEngine({ module() }) {
        detector.stub {
            on { detect(any()) } doReturn(listOf(Rect(2, 3, 4, 5)))
        }

        extractor.stub {
            on { batch(any()) } doReturn (listOf("De ontdekking van Harry Mulisch"))
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn (listOf(
                Book(
                    id = "test",
                    identifiers = mapOf(Identifier.INTERNAL to "test"),
                    title = "The ontdekking van de hemel",
                    authors = listOf("Harry Mulisch")
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentLength, "bla")
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: DetectionResult? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.size)
        }
    }

    @Test
    fun `post handles incorrect estimation`() = withTestEngine({ module() }) {
        detector.stub {
            on { detect(any()) } doReturn(listOf(Rect(2, 3, 4, 5)))
        }

        extractor.stub {
            on { batch(any()) } doReturn (listOf("De ontdekking van Harry Mulisch"))
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn (listOf(
                Book(
                    id = "test",
                    identifiers = mapOf(Identifier.INTERNAL to "test"),
                    title = "The ontdekking van de hemel",
                    authors = listOf("Harry Mulisch")
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentLength, "-1")
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: DetectionResult? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.size)
        }
    }

    @Test
    fun `post handles internal server error`() = withTestEngine({ module() }) {
        extractor.stub {
            on { batch(any()) } doThrow RuntimeException("This is staged.")
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn (listOf(
                Book(
                    id = "test",
                    identifiers = mapOf(Identifier.INTERNAL to "test"),
                    title = "The ontdekking van de hemel",
                    authors = listOf("Harry Mulisch")
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
            val response: DetectionFailure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("server_error", response?.type)
        }
    }

    @Test
    fun `non-post method not allowed`() = withTestEngine({ module() }) {
        val request = handleRequest(HttpMethod.Get, "/api/detection") {
            configureAuthorization("test", listOf("detection"))
        }
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    private fun Application.module() {
        val context = createTestContext {
            beans {
                // Application routes
                bean { { routing: Routing -> routing.routes() } }
                bean { detector }
                bean { extractor }
                bean { catalogue }

                // VisionService
                bean { VisionService(ref(), ref(), ref()) }
            }.initialize(this)
        }

        context.bootstrap(this) { booklab() }
    }

    /**
     * The routes of the application.
     */
    private fun Routing.routes() {
        route("/api/detection") { detection() }
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
