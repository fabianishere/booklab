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

package nl.tudelft.booklab.vision.ocr.tesseract

import org.bytedeco.javacpp.lept.pixRead
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class BasicTesseractExampleTest {
    @Test
    fun `given TessBaseApi when image OCRd then text displayed`() {
        val data = BasicTesseractExampleTest::class.java.getResourceAsStream("/tesseract/languages/english")
        Tesseract(data, emptyMap()).use { api ->
            // Open input image with leptonica library
            val buffer = BasicTesseractExampleTest::class.java.getResourceAsStream("/tesseract.png")
            val tmp = Files.createTempFile("tesseract-image", ".png")
            Files.copy(buffer, tmp, StandardCopyOption.REPLACE_EXISTING)
            val image = pixRead(tmp.toString())
            Files.delete(tmp)

            // Get OCR result
            val string = api.extract(image)
            assertFalse(string.isEmpty())

            println("OCR output:")
            println(string)
        }
    }
}
