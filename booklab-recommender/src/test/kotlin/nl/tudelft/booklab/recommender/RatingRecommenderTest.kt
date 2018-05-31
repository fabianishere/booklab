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
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.recommender.rating.RatingRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RatingRecommenderTest {
    lateinit var recommender: RatingRecommender

    @BeforeEach
    fun setup() {
        val key = System.getenv()["GOODREADS_API_KEY"]
        assumeTrue(key != null, "No Goodreads API key is given (key GOODREADS_API_KEY)")
        recommender = RatingRecommender(key = key!!)
    }

    @Test
    fun `default test`() {
        val candidates = listOf(
            Book(listOf(Title("harry potter")), emptyList(), listOf("9788700631625")),
            Book(listOf(Title("kaas")), emptyList(), listOf("9789025363758")),
            Book(listOf(Title("dit zijn de namen")), emptyList(), listOf("9789023473282")),
            Book(listOf(Title("de ontdekking van de hemel")), emptyList(), listOf("9789023443988"))
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
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225")),
            Book(listOf(Title("kaas")), emptyList(), listOf("9789025363758")),
            Book(listOf(Title("dit zijn de namen")), emptyList(), listOf("9789023473282")),
            Book(listOf(Title("de ontdekking van de hemel")), emptyList(), listOf("9789023443988"))
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
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225"))
        )
        val candidates = listOf(
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225")),
            Book(listOf(Title("kaas")), emptyList(), listOf("9789025363758")),
            Book(listOf(Title("dit zijn de namen")), emptyList(), listOf("9789023473282")),
            Book(listOf(Title("de ontdekking van de hemel")), emptyList(), listOf("9789023443988"))
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(3, results.size)
        }
    }

    @Test
    fun `remove duplicates from candidates`() {
        val candidates = listOf(
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225")),
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225")),
            Book(listOf(Title("kaas")), emptyList(), listOf("9789025363758")),
            Book(listOf(Title("dit zijn de namen")), emptyList(), listOf("9789023473282")),
            Book(listOf(Title("de ontdekking van de hemel")), emptyList(), listOf("9789023443988"))
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(4, results.size)
        }
    }

    @Test
    fun `unknown books are least recommended`() {
        val candidates = listOf(
            Book(listOf(Title("harry potter")), emptyList(), listOf("0545010225")),
            Book(listOf(Title("UNKNOWN 1")), emptyList(), listOf("123")),
            Book(listOf(Title("UNKNOWN 2")), emptyList(), listOf("234")),
            Book(listOf(Title("kaas")), emptyList(), listOf("9789025363758")),
            Book(listOf(Title("dit zijn de namen")), emptyList(), listOf("9789023473282")),
            Book(listOf(Title("de ontdekking van de hemel")), emptyList(), listOf("9789023443988"))
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(6, results.size)
            assertEquals(results[0], candidates[0])
            assertEquals(results[1], candidates[5])
            assertEquals(results[2], candidates[4])
            assertEquals(results[3], candidates[3])
            assertEquals(results[4], candidates[1])
            assertEquals(results[5], candidates[2])
        }
    }
}
