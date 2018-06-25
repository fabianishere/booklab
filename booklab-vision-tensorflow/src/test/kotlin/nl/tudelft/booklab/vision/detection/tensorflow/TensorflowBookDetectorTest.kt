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

package nl.tudelft.booklab.vision.detection.tensorflow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.tensorflow.Graph

class TensorflowBookDetectorTest {
    lateinit var detector: TensorflowBookDetector

    @BeforeEach
    fun setUp() {
        detector = TensorflowBookDetector(graph)
    }

    @Test
    fun `tensorflow can detect books in JPEG image`() {
        val buffer = TensorflowBookDetectorTest::class.java.getResourceAsStream("/bookshelf.jpg").readBytes()
        val mat = Imgcodecs.imdecode(MatOfByte(*buffer), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val books = detector.detect(mat)
        assertEquals(36, books.size, "The amount of books in the image differed")
    }

    @Test
    fun `tensorflow can detect books in PNG image`() {
        val buffer = TensorflowBookDetectorTest::class.java.getResourceAsStream("/bookshelf.png").readBytes()
        val mat = Imgcodecs.imdecode(MatOfByte(*buffer), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val books = detector.detect(mat)
        assertEquals(36, books.size, "The amount of books in the image differed")
    }

    companion object {
        lateinit var graph: Graph

        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }

        @BeforeAll
        @JvmStatic
        fun setUpClass() {
            graph = Graph()
            val data = TensorflowBookDetector::class.java.getResourceAsStream("/tensorflow/inception-book-model.pb")
                .use { it.readBytes() }
            graph.importGraphDef(data)
        }
    }
}
