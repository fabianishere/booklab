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
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.services.vision.BookDetection
import nl.tudelft.booklab.backend.services.vision.VisionService
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.withTestEngine
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
import nl.tudelft.booklab.catalogue.Book as AbstractBook

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
     * The catalogue service to use.
     */
    private lateinit var catalogue: CatalogueService

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
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn listOf(book)
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.OK, response.status())
            val response: ApiResponse.Success<List<BookDetection>>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.data?.size)
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

        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn listOf(book)
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
            val response: ApiResponse.Success<List<BookDetection>>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.data?.size)
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
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn listOf(book)
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
            val response: ApiResponse.Success<List<BookDetection>>? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals(1, response?.data?.size)
        }
    }

    @Test
    fun `post handles internal server error`() = withTestEngine({ module() }) {
        extractor.stub {
            on { batch(any()) } doThrow RuntimeException("This is staged.")
        }
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.INTERNAL to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn listOf(book)
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
            configureAuthorization("test", listOf("detection"))
            addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
        }
        with(request) {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
            val response: ApiResponse.Failure? = response.content?.let { mapper.readValue(it) }
            assertNotNull(response)
            assertEquals("server_error", response?.error?.code)
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
                bean("routes") { Routes.from { routes() } }

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
