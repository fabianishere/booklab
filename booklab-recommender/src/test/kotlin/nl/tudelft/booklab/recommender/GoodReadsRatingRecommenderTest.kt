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
import nl.tudelft.booklab.recommender.rating.goodreads.GoodreadsRatingRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GoodReadsRatingRecommenderTest {
    private lateinit var recommender: GoodreadsRatingRecommender

    @BeforeEach
    fun setup() {
        val key = System.getenv()["GOODREADS_API_KEY"]
        assumeTrue(key != null, "No Goodreads API key is given (key GOODREADS_API_KEY)")
        recommender = GoodreadsRatingRecommender(key = key!!)
    }

    @Test
    fun `default test`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList())
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(results.size, 4)
            assertEquals(results[0], candidates[0])
            assertEquals(results[1], candidates[3])
            assertEquals(results[2], candidates[2])
            assertEquals(results[3], candidates[1])
        }
    }

    @Test
    fun `isbn 10 and isbn 13 can be mixed`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList())
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(results.size, 4)
            assertEquals(results[0], candidates[0])
            assertEquals(results[1], candidates[3])
            assertEquals(results[2], candidates[2])
            assertEquals(results[3], candidates[1])
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
            TestBook(mapOf(Identifier.ISBN_10 to "0545010225"), "harry potter", emptyList())
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList())
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(3, results.size)
        }
    }

    @Test
    fun `remove duplicates from candidates`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_10 to "0545010225"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "0545010225"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList())
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(4, results.size)
        }
    }

    @Test
    fun `books with no rating are discarded`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_10 to "0545010225"), "harry potter", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "234"), "UNKNOWN 1", emptyList()),
            TestBook(mapOf(Identifier.ISBN_10 to "234"), "UNKNOWN 2", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList()),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList())
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(4, results.size)
            assertEquals(results[0], candidates[0])
            assertEquals(results[1], candidates[5])
            assertEquals(results[2], candidates[4])
            assertEquals(results[3], candidates[3])
        }
    }
}
