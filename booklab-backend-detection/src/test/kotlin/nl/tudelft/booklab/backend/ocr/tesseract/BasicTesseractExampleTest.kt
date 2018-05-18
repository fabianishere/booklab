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

package nl.tudelft.booklab.backend.ocr.tesseract;

import org.bytedeco.javacpp.lept.pixDestroy
import org.bytedeco.javacpp.lept.pixRead
import org.bytedeco.javacpp.tesseract
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class BasicTesseractExampleTest {
    @Test
    fun `given TessBaseApi when image OCRd then text displayed`() {
        val api = tesseract.TessBaseAPI()
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            throw IllegalStateException("Failed to initialise Tesseract")
        }

        // Open input image with leptonica library
        val path = BasicTesseractExampleTest::class.java.getResource("/tesseract.png");
        val pathString = path.getPath().substring(1);
        val image = pixRead(pathString);

        api.SetImage(image);
        // Get OCR result
        val outText = api.GetUTF8Text();
        val string = outText.getString();
        assertFalse(string.isEmpty());

        println("OCR output:")
        println(string)

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);
    }
}
