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

package nl.tudelft.booklab.vision

import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_UNCHANGED
import org.opencv.imgcodecs.Imgcodecs.imdecode
import java.io.InputStream

/**
 * Convert the given [InputStream] to an OpenCV matrix.
 *
 * @param estimatedSize The estimated size of input stream.
 */
fun InputStream.toMat(estimatedSize: Int = DEFAULT_BUFFER_SIZE) =
    imdecode(MatOfByte(*readBytes(estimatedSize)), CV_LOAD_IMAGE_UNCHANGED)
