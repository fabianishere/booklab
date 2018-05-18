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

import nl.tudelft.nlbooklab.backend.ocr.tesseract.OCRPreprocessor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opencv.core.Core
import org.opencv.core.Core.randu
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR
import org.opencv.imgproc.Imgproc.cvtColor

internal class OCRPreprocessorTest {
    @Test
    fun `test OCR preprocessor`() {
        val image = Mat(100, 100, CvType.CV_8UC3)
        randu(image, 0.0, 256.0)
        val optimized = OCRPreprocessor.optimizeImg(image)

        cvtColor(optimized, optimized, COLOR_GRAY2BGR)

        val diff = Mat()
        Core.absdiff(image, optimized, diff)

        val sum = Core.sumElems(diff)
        assertTrue(sum.`val`[0] + sum.`val`[1] + sum.`val`[2] + sum.`val`[3] > 0)
    }

    companion object {

        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
