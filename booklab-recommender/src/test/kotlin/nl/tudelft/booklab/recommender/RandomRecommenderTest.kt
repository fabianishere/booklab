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
import nl.tudelft.booklab.recommender.random.RandomRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random

class RandomRecommenderTest {
    private lateinit var recommender: Recommender

    @BeforeEach
    fun setUp() {
        recommender = RandomRecommender(Random(123))
    }

    @Test
    fun `default test`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "title 1", listOf("author 1")),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "title 2", listOf("author 2")),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "title 3", listOf("author 2")),
            TestBook(mapOf(Identifier.INTERNAL to "4"), "title 4", listOf("author 2")),
            TestBook(mapOf(Identifier.INTERNAL to "5"), "title 5", listOf("author 3")),
            TestBook(mapOf(Identifier.INTERNAL to "6"), "title 6", listOf("author 3"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "7"), "title 7", listOf("author 1")),
            TestBook(mapOf(Identifier.INTERNAL to "8"), "title 8", listOf("author 2")),
            TestBook(mapOf(Identifier.INTERNAL to "9"), "title 9", listOf("author 3"))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), candidates.toSet()) }

        assertEquals(candidates[1], results[0])
        assertEquals(candidates[0], results[1])
        assertEquals(candidates[2], results[2])
    }

    @Test
    fun `collected books are discarded from candidates`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789025363758"), "kaas", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(1.0, 1))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9789023473282"), "dit zijn de namen", emptyList(), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.ISBN_13 to "9789023443988"), "de ontdekking van de hemel", emptyList(), ratings = Ratings(1.0, 1))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), candidates.toSet()) }

        assertEquals(1, results.size)
    }

    @Test
    fun `empty collection returns all the candidates`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1))

        )

        val results = runBlocking { recommender.recommend(emptySet(), candidates.toSet()) }

        assertEquals(1, results.size)
    }

    @Test
    fun `no candidates returns a empty list`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.ISBN_13 to "9788700631625"), "harry potter", emptyList(), ratings = Ratings(1.0, 1))
        )
        val results = runBlocking { recommender.recommend(collection.toSet(), emptySet()) }

        assertTrue(results.isEmpty())
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
}
