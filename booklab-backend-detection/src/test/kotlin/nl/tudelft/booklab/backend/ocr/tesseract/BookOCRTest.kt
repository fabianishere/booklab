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

package nl.tudelft.booklab.backend.ocr.tesseract

import nl.tudelft.nlbooklab.backend.ocr.tesseract.BookOCRTesseract
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BookOCRTest {
    @Test
    fun `smoke test`() {
        val stream = BookOCRTest::class.java.getResourceAsStream("/bookshelf.jpg")
        val ocr = BookOCRTesseract()
        val res = ocr.getBookList(stream)
        assertTrue(res.size > 0)
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
