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

import nl.tudelft.nlbooklab.backend.ocr.ImgProcessHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.opencv.core.Core
import org.opencv.core.Core.randu
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar

internal class ImgProcessHelperTest {
    @Test
    fun `test colorhist equalize`() {
        val image = Mat(100, 100, CvType.CV_8UC3)
        randu(image, 0.0, 256.0)
        val mat = ImgProcessHelper.colorhistEqualize(image)

        assertNotEquals(Core.sumElems(image), Core.sumElems(mat))
    }

    @Test
    fun `test autocanny`() {
        val image = Mat(100, 100, CvType.CV_8UC3)
        randu(image, 0.0, 256.0)
        val canny = ImgProcessHelper.autoCanny(image)

        assertNotEquals(0, Core.sumElems(canny))
    }

    @Test
    fun `test median`() {
        val image = Mat(10, 10, CvType.CV_8UC3, Scalar(1.0, 2.0, 0.0))
        assertEquals(1, ImgProcessHelper.getMedian(image))
    }

    @Test
    fun `matrix can be converted to pix buffer`() {
        val image = Mat(10, 10, CvType.CV_8UC3, Scalar(1.0, 1.0, 1.0))
        val testpix = ImgProcessHelper.convertMatToPix(image)
        assertEquals(10, testpix.w())
        assertEquals(10, testpix.h())
    }

    @Test
    fun `test hough lines`() {
        val image = Mat(100, 100, CvType.CV_8UC3)
        randu(image, 0.0, 256.0)
        val lines = ImgProcessHelper.detectBookHoughLines(image)

        assertNotEquals(0, Core.sumElems(lines))
    }

    companion object {
        init {
            nu.pattern.OpenCV.loadShared()
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME)
        }
    }
}
