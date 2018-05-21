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
import org.junit.jupiter.api.Assertions.assertTrue
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs

class VisionTextExtractorTest {
    // I have disabled this test for now until we can integrate the Google Cloud Vision tests into the CI/CD pipeline.
    // @Test
    fun `smoke test`() {
        val client = ImageAnnotatorClient.create()
        val stream = GoogleVisionTextExtractor::class.java.getResourceAsStream("/bookshelf.jpg")
        val image = Imgcodecs.imdecode(MatOfByte(*stream.readBytes()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val visionOCR = GoogleVisionTextExtractor(client)
        val res = visionOCR.extract(image)
        assertTrue(res.isNotEmpty())
        println(res)
        client.close()
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
