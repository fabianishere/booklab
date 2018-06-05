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
import nl.tudelft.booklab.recommender.hybrid.StrictHybridRecommender
import nl.tudelft.booklab.recommender.random.RandomRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random

class StrictHybridRecommenderTest {
    private lateinit var recommender: StrictHybridRecommender

    @BeforeEach
    fun setup() {
        recommender = StrictHybridRecommender()
    }

    @Test
    fun `default test`() {
        val collection = listOf(
            Book(listOf(Title("title 0")), listOf("GREAT_AUTHOR"), listOf("123"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 0")), listOf("GREAT_AUTHOR"), listOf("123")),
            Book(listOf(Title("title 1")), listOf("author"), listOf("234"), 5.0),
            Book(listOf(Title("title 2")), listOf("author"), listOf("345"), 3.0),
            Book(listOf(Title("title 3")), listOf("author"), listOf("456"), 2.0),
            Book(listOf(Title("title 4")), listOf("GREAT_AUTHOR"), listOf("567"), 2.5),
            Book(listOf(Title("title 5")), listOf("GREAT_AUTHOR"), listOf("678"), 3.5),
            Book(listOf(Title("title 6")), listOf("author"), listOf("789"))
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(6, results.size)
            assertEquals(candidates[5], results[0])
            assertEquals(candidates[4], results[1])
            assertEquals(candidates[1], results[2])
            assertEquals(candidates[2], results[3])
            assertEquals(candidates[3], results[4])
            assertEquals(candidates[6], results[5])
        }
    }

    @Test
    fun `no candidates is no results`() {
        val collection = listOf(
            Book(listOf(Title("title 0")), listOf("GREAT_AUTHOR"), listOf("123"))
        )

        runBlocking { assertTrue(recommender.recommend(collection.toSet(), emptySet()).isEmpty()) }
    }

    @Test
    fun `no collection does not fail`() {
        val candidates = listOf(
            Book(listOf(Title("title 0")), listOf("author"), listOf("123")),
            Book(listOf(Title("title 1")), listOf("author"), listOf("234")),
            Book(listOf(Title("title 2")), listOf("author"), listOf("345")),
            Book(listOf(Title("title 3")), listOf("author"), listOf("456")),
            Book(listOf(Title("title 4")), listOf("author"), listOf("567")),
            Book(listOf(Title("title 5")), listOf("author"), listOf("678")),
            Book(listOf(Title("title 6")), listOf("author"), listOf("789"))
        )

        runBlocking { assertEquals(7, recommender.recommend(emptySet(), candidates.toSet()).size) }
    }

    @Test
    fun `authors are ranked higher than ratings`() {
        val collection = listOf(
            Book(listOf(Title("title 3")), listOf("GREAT_AUTHOR"), listOf("666"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 1")), listOf("author"), listOf("234"), 5.0),
            Book(listOf(Title("title 0")), listOf("GREAT_AUTHOR"), listOf("123"))
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[0], results[1])
        }
    }

    @Test
    fun `ratings are ranked higher than random other recommendations`() {
        val candidates = listOf(
            Book(listOf(Title("title 1")), listOf("author"), listOf("234")),
            Book(listOf(Title("title 0")), listOf("author"), listOf("123"), 3.3)
        )

        runBlocking {
            val results = recommender.recommend(emptySet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[0], results[1])
        }
    }

    @Test
    fun `if no recommendations are possible give random recommendations`() {
        val candidates = listOf(
            Book(listOf(Title("title 0")), listOf("author"), listOf("123")),
            Book(listOf(Title("title 1")), listOf("author"), listOf("234")),
            Book(listOf(Title("title 2")), listOf("author"), listOf("345"))
        )

        runBlocking {
            val results = StrictHybridRecommender(randomRecommender = RandomRecommender(Random(123)))
                .recommend(emptySet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[0], results[1])
            assertEquals(candidates[2], results[2])
        }
    }
}
