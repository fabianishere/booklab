/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.booklab.recommender

import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.catalogue.Ratings
import nl.tudelft.booklab.recommender.rating.google.GoogleBooksRatingRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GoogleBooksRatingRecommenderTest {
    private val recommender = GoogleBooksRatingRecommender()

    @Test
    fun `default test`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(4.5, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList(), ratings = Ratings(4.3, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList(), ratings = Ratings(4.8, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(3.3, 1))
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(results.size, 4)
            assertEquals(results[0], candidates[2])
            assertEquals(results[1], candidates[0])
            assertEquals(results[2], candidates[1])
            assertEquals(results[3], candidates[3])
        }
    }

    @Test
    fun `collection is discarded from results`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.ISBN_10 to "isbn 1"), "title 1", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "isbn 2"), "title 2", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "isbn 3"), "title 3", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "isbn 4"), "title 4", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "isbn 5"), "title 5", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(1.0, 1))
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(3, results.size)
        }
    }

    @Test
    fun `remove duplicates from candidates`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(1.0, 1))
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(4, results.size)
        }
    }

    @Test
    fun `books with no rating are discarded`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(1.0, 1))
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(3, results.size)
        }
    }
}
