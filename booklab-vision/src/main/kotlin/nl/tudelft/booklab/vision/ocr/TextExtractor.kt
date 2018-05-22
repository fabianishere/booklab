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

package nl.tudelft.booklab.vision.ocr

import org.opencv.core.Mat

/**
 * Implementors of this interface provide a way to extract text from the specified OpenCV matrix.
 */
interface TextExtractor {
    /**
     * Extract the text from the given image represented as OpenCV matrix.
     *
     * @param mat The matrix to extract the text from.
     * @return The list of strings that have been extracted from the image.
     */
    fun extract(mat: Mat): List<String>

    /**
     * Extract the text from a batch of images.
     *
     * @param matrices The matrices to extract the text from.
     * @return A list of strings that have been extract per image.
     */
    fun batch(matrices: List<Mat>): List<List<String>> = matrices.map { extract(it) }
}
