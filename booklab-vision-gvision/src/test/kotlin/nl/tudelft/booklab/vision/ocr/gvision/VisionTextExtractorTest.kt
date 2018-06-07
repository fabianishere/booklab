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

package nl.tudelft.booklab.vision.ocr.gvision

import com.google.cloud.vision.v1.ImageAnnotatorClient
import nl.tudelft.booklab.vision.detection.opencv.GoogleVisionBookDetector
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs

class VisionTextExtractorTest {
    lateinit var client: ImageAnnotatorClient

    @BeforeEach
    fun setUp() {
        client = try {
            ImageAnnotatorClient.create()
        } catch (e: Throwable) {
            assumeTrue(false, "No Google Cloud credentials available for running the Google Vision tests.")
            throw e
        }
    }

    // I have disabled this test for now until we can integrate the Google Cloud Vision tests into the CI/CD pipeline.
    @Test
    fun `smoke test`() {
        val stream = GoogleVisionTextExtractor::class.java.getResourceAsStream("/bookshelf.jpg")
        val image = Imgcodecs.imdecode(MatOfByte(*stream.readBytes()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val visionOCR = GoogleVisionTextExtractor(client)
        val res = visionOCR.extract(image)
        assertTrue(res.isNotEmpty())
        println(res)
        client.close()
    }

    @Test
    fun `book recognition test`() {
        val stream = GoogleVisionTextExtractor::class.java.getResourceAsStream("/bookshelf.jpg")
        val image = Imgcodecs.imdecode(MatOfByte(*stream.readBytes()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val textExtractor = GoogleVisionTextExtractor(client)

        val books = GoogleVisionBookDetector(client).detect(image)
        val bookText = textExtractor.batch(books)

        bookText.forEach(System.out::println)

        assertTrue(bookText.isNotEmpty())
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
