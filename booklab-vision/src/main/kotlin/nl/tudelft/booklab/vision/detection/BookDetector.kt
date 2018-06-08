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

package nl.tudelft.booklab.vision.detection

import org.opencv.core.Mat
import org.opencv.core.Rect

/**
 * Implementors of this interface provide a strategy for detecting books
 */
interface BookDetector {
    /**
     * Detect the given book in the image represented as OpenCV matrix.
     *
     * @param mat The image represented as matrix to detect the books in.
     * @return A list of rectangles that represents the locations of the detected books.
     */
    fun detect(mat: Mat): List<Rect>
}
