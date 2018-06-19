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

import org.bytedeco.javacpp.lept
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs

/**
 * Convert an OpenCV matrix into a Leptonica pixel buffer.
 *
 * @return The Leptonica pixel buffer.
 */
fun Mat.toPix(): lept.PIX {
    val bytes = MatOfByte()
    Imgcodecs.imencode(".tiff", this, bytes)
    val buffer = bytes.toArray()
    return lept.pixReadMem(buffer, buffer.size.toLong())
}
