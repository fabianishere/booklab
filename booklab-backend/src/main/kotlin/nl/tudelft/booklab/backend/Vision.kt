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

package nl.tudelft.booklab.backend

import io.ktor.util.AttributeKey
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor

/**
 * The configuration used for detecting and identifying books.
 *
 * @property detector The detector to detect the books in an image.
 * @property extractor The extractor to extract the text from the image.
 * @property catalogue The catalogue configuration to cross match the books.
 */
data class VisionConfiguration(val detector: BookDetector, val extractor: TextExtractor, val catalogue: CatalogueConfiguration) {
    companion object {
        /**
         * The attribute key that allows the user to access the [VisionConfiguration] object within an application.
         */
        val KEY = AttributeKey<VisionConfiguration>("VisionConfiguration")
    }
}
