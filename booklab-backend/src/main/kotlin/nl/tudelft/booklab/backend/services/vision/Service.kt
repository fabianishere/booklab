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

package nl.tudelft.booklab.backend.services.vision

import kotlinx.coroutines.experimental.async
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.vision.detection.BookDetector
import nl.tudelft.booklab.vision.ocr.TextExtractor
import org.opencv.core.Mat
import org.opencv.core.Rect

/**
 * A service used for detecting and identifying books.
 *
 * @property detector The detector to detect the books in an image.
 * @property extractor The extractor to extract the text from the image.
 * @property catalogue The catalogue configuration to cross match the books.
 */
class VisionService(
    private val detector: BookDetector,
    private val extractor: TextExtractor,
    private val catalogue: CatalogueClient
) {
    /**
     * Detect the books in the given image.
     *
     * @param image The image to detect the books in.
     * @return A list of books that has been detected.
     */
    suspend fun detect(image: Mat): List<BookDetection> {
        val segments = detector.detect(image)
        val text = extractor.batch(segments.map { Mat(image, it) })
        return segments
            .zip(text)
            .map { (segment, part) ->
                async {
                    part
                        .takeUnless { it.isBlank() }
                        ?.let { catalogue.query(it, max = 1) }
                        ?.let { BookDetection(segment, it) }
                }
            }
            .mapNotNull { it.await() }
    }
}

/**
 * This class defines the shape of a detected book result.
 *
 * @property box The rectangle where the book was detected.
 * @property matches A list of actual books that have been matched.
 */
data class BookDetection(val box: Rect, val matches: List<Book>)
