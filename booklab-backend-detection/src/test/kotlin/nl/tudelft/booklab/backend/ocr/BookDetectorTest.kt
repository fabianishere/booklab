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

package nl.tudelft.booklab.backend.ocr

import nl.tudelft.nlbooklab.backend.ocr.BookDetector
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs

class BookDetectorTest {
    @Test
    fun `smoke test`() {
        val buffer = BookDetectorTest::class.java.getResourceAsStream("/bookshelf.jpg").readBytes()
        val mat = Imgcodecs.imdecode(MatOfByte(*buffer), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        val books = BookDetector.detectBooks(mat)

        assertTrue(books.size > 0)
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
