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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.books.Books
import com.google.api.services.books.BooksRequestInitializer
import com.google.cloud.vision.v1.ImageAnnotatorClient
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
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import nl.tudelft.booklab.backend.CatalogueConfiguration
import nl.tudelft.booklab.backend.VisionConfiguration
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.google.GoogleCatalogueClient
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.detection.opencv.GoogleVisionBookDetector
import nl.tudelft.booklab.vision.detection.tensorflow.TensorflowBookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor
import nl.tudelft.booklab.vision.ocr.gvision.GoogleVisionTextExtractor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.tensorflow.Graph
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/**
 * This class provides a benchmark for the book recognition algorithms which we use to evaluate the
 * accuracy of the algorithms.
 *
 */
@Tag("benchmark")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
sealed class DetectionBenchmark {
    /**
     * The Jackson mapper class that maps JSON to objects.
     */
    private lateinit var mapper: ObjectMapper

    /**
     * The test environment to use.
     */
    private lateinit var engine: TestApplicationEngine

    /**
     * The amount of books that have been correctly found.
     */
    private var correct = 0

    /**
     * The amount of books that have been processed.
     */
    private var total = 0

    /**
     * The amount of books that we incorrectly guessed.
     */
    private var incorrect = 0

    /**
     * Set up the benchmark suite
     */
    @BeforeAll
    fun internalSetupClass() {
        setUpClass()

        mapper = jacksonObjectMapper()
        engine = TestApplicationEngine(detectionEnvironment())
        engine.start()
    }

    /**
     * A function to initialise the server.
     */
    open fun setUpClass() {}

    /**
     * Tear down the benchmark suite
     */
    @AfterAll
    fun internalTearDownClass() {
        println("Total score: $correct/$total ($incorrect incorrect)")

        engine.stop(0L, 0L, TimeUnit.MILLISECONDS)
    }

    /**
     * Method to configure the server
     */
    open fun detectionModule(application: Application) {
        application.run {
            install(ContentNegotiation) {
                jackson {
                    configure(SerializationFeature.INDENT_OUTPUT, true)
                    registerModule(JavaTimeModule())
                }
            }
        }
    }

    /**
     * Helper method to create a Ktor test environment.
     */
    private fun detectionEnvironment() = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        module { detectionModule(this) }
    }

    /**
     * The actual test that runs a couple of images through the detector.
     */
    @ParameterizedTest
    @CsvFileSource(resources = ["/benchmark/detection/configurations.csv"])
    fun `some correct books are retrieved`(bookshelf: String, bookTitles: String, authors: String) = with(engine) {
        val titles = DetectionBenchmark::class.java.getResourceAsStream(bookTitles).reader().useLines { it.toList() }
        val image = DetectionBenchmark::class.java.getResourceAsStream(bookshelf).readBytes()

        val timing = measureTimeMillis {
            val request = handleRequest(HttpMethod.Post, "/api/detection") {
                setBody(image)
                addHeader(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
            }
            with(request) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                val response: DetectionResult? = response.content?.let { mapper.readValue(it) }
                val responseTitles = response?.results?.map { book -> book.titles[0].value }
                val intersection = titles.intersect(responseTitles!!)

                correct += intersection.size
                total += titles.size
                incorrect += responseTitles.size - intersection.size
                println("Found: $responseTitles")
                println("Needed: $titles")
                println("Correct: $intersection")
                println("Score: ${intersection.size}/${titles.size} (${responseTitles.size - intersection.size} incorrect)")
            }
        }

        println("Took: $timing ms")
    }

    /**
     * A benchmark for the Google Vision implementations
     */
    open class GoogleVisionBenchmark : DetectionBenchmark() {
        private lateinit var catalogue: CatalogueClient
        protected lateinit var detector: BookDetector
        private lateinit var extractor: TextExtractor

        override fun setUpClass() {
            super.setUpClass()
            catalogue = setUpBooks()

            val vision = setUpVision()
            detector = GoogleVisionBookDetector(vision)
            extractor = GoogleVisionTextExtractor(vision)
        }

        /**
         * Create a Google Books API client.
         */
        private fun setUpBooks(): CatalogueClient {
            // Setup Google Books catalogue
            val key = System.getenv()["GOOGLE_BOOKS_API_KEY"]
            assumeTrue(key != null, "No Google Books API key given for running the Google Books tests (key GOOGLE_BOOKS_API_KEY)")
            return GoogleCatalogueClient(
                Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("booklab")
                    .setGoogleClientRequestInitializer(BooksRequestInitializer(key))
                    .build()
            )
        }

        /**
         * Create a Google Vision [ImageAnnotatorClient].
         */
        private fun setUpVision(): ImageAnnotatorClient = try {
            ImageAnnotatorClient.create()
        } catch (e: Throwable) {
            e.printStackTrace()
            assumeTrue(false, "No Google Cloud credentials available for running the Google Vision tests.")
            throw e
        }

        override fun detectionModule(application: Application) {
            super.detectionModule(application)
            application.run {
                routing {
                    route("/api/detection") {
                        detection(
                            VisionConfiguration(
                                detector = detector,
                                extractor = extractor,
                                catalogue = CatalogueConfiguration(catalogue)
                                    .also {
                                        attributes.put(CatalogueConfiguration.KEY, it)
                                    }
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * A benchmark of the Tensorflow implementation
     */
    class TensorflowBenchmark : GoogleVisionBenchmark() {
        private lateinit var graph: Graph

        override fun setUpClass() {
            super.setUpClass()

            graph = setUpTensorflow()
            detector = TensorflowBookDetector(graph)
        }

        private fun setUpTensorflow(): Graph = Graph().also {
            val data = TensorflowBookDetector::class.java.getResourceAsStream("/tensorflow/inception-book-model.pb")
                .use { it.readBytes() }
            it.importGraphDef(data)
        }
    }
}
