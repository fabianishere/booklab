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

package nl.tudelft.booklab.backend.services.vision

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.backend.api.v1.Book
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import kotlin.test.assertEquals

/**
 * Test suite for the [VisionService] class.
 */
internal class VisionServiceTest {
    /**
     * The [BookDetector] to use.
     */
    private lateinit var detector: BookDetector

    /**
     * The [TextExtractor] to use.
     */
    private lateinit var extractor: TextExtractor

    /**
     * The [CatalogueService] to use.
     */
    private lateinit var catalogue: CatalogueClient

    /**
     * The [VisionService] to test.
     */
    private lateinit var service: VisionService

    /**
     * Dummy book instance.
     */
    private val book = Book(
        id = "test",
        identifiers = mapOf(Identifier.INTERNAL to "test"),
        title = "The ontdekking van de hemel",
        authors = listOf("Harry Mulisch")
    )

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        detector = mock()
        extractor = mock()
        catalogue = mock()
        service = VisionService(detector, extractor, catalogue)
    }

    @Test
    fun `detect should ignore empty text regions`() {
        val mat = Mat(Size(10.0, 10.0), 8)
        detector.stub {
            on { detect(any()) } doReturn listOf(Rect(1, 2, 3, 4))
        }

        extractor.stub {
            on { batch(any()) } doReturn listOf("")
        }

        runBlocking {
            assertEquals(0, service.detect(mat).size)
        }
    }

    @Test
    fun `detect should not return duplicates`() {
        val mat = Mat(Size(10.0, 10.0), 8)
        detector.stub {
            on { detect(any()) } doReturn listOf(Rect(1, 2, 3, 4))
        }

        extractor.stub {
            on { batch(any()) } doReturn listOf("test-1", "test-2")
        }

        catalogue.stub {
            onBlocking { query(any(), any<Int>()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(1, service.detect(mat).size)
        }
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
