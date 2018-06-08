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
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withApplication
import nl.tudelft.booklab.backend.CatalogueConfiguration
import nl.tudelft.booklab.backend.VisionConfiguration
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.catalogue.TitleType
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.opencv.core.Mat

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
     * The book detector to use.
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
        detector = mock {
            on { detect(any()) } doReturn(emptyList<Mat>())
        }
        extractor = mock()
        catalogue = mock()
    }

    @Test
    fun `post returns proper interface`() = withApplication(detectionEnvironment()) {
        extractor.stub {
            on { batch(any()) } doReturn(listOf("De ontdekking van Harry Mulisch"))
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn(listOf(
                Book(
                    titles = listOf(Title("The ontdekking van de hemel", TitleType.MAIN)),
                    authors = listOf("Harry Mulisch"),
                    ids = emptyList()
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
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
    fun `post does not return duplicates`() = withApplication(detectionEnvironment()) {
        extractor.stub {
            on { batch(any()) } doReturn(listOf("De ontdekking van", "Harry Mulisch"))
        }
        catalogue.stub {
            onBlocking { query(anyString(), anyInt()) } doReturn(listOf(
                Book(
                    titles = listOf(Title("The ontdekking van de hemel", TitleType.MAIN)),
                    authors = listOf("Harry Mulisch"),
                    ids = emptyList()
                )
            ))
        }

        val image = DetectionTest::class.java.getResourceAsStream("/test-image.jpg").readBytes()
        val request = handleRequest(HttpMethod.Post, "/api/detection") {
            setBody(image)
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
    fun `non-post method not allowed`() = withApplication(detectionEnvironment()) {
        val request = handleRequest(HttpMethod.Get, "/api/detection")
        with(request) {
            assertEquals(HttpStatusCode.MethodNotAllowed, response.status())
        }
    }

    private fun detectionEnvironment() = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        module { detectionModule() }
    }

    private fun Application.detectionModule() {
        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)
                registerModule(JavaTimeModule())
            }
        }

        routing {
            route("/api/detection") {
                detection(
                    VisionConfiguration(
                        detector = detector,
                        extractor = extractor,
                        catalogue = CatalogueConfiguration(catalogue)
                    )
                )
            }
        }
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
