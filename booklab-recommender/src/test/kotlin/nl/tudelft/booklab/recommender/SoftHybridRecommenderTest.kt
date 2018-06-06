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
import nl.tudelft.booklab.recommender.hybrid.SoftHybridRecommender
import nl.tudelft.booklab.recommender.random.RandomRecommender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Random

class SoftHybridRecommenderTest {
    private lateinit var recommender: SoftHybridRecommender

    @BeforeEach
    fun setup() {
        recommender = SoftHybridRecommender(softness = 2.0)
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
            Book(listOf(Title("title 1")), listOf("author"), listOf("234"))
        )

        runBlocking { assertEquals(2, recommender.recommend(emptySet(), candidates.toSet()).size) }
    }

    @Test
    fun `if both books have a rating and a recommended author favor the rating`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR"), rating = 4.0),
            Book(listOf(Title("book 1")), listOf("GREAT_AUTHOR"), rating = 5.0)
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[0], results[1])
        }
    }

    @Test
    fun `softness can overrules author recommendation when rating is high enough`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR"), rating = 2.0),
            Book(listOf(Title("book 1")), listOf("author"), rating = 5.0),
            Book(listOf(Title("book 2")), listOf("author"), rating = 3.0)
        )

        runBlocking {
            val results = SoftHybridRecommender(softness = 2.0).recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[0], results[1])
            assertEquals(candidates[2], results[2])
        }
    }

    @Test
    fun `higher rating means more recommended`() {
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("author"), rating = 2.0),
            Book(listOf(Title("book 1")), listOf("author"), rating = 5.0),
            Book(listOf(Title("book 2")), listOf("author"), rating = 3.0)
        )

        runBlocking {
            val results = SoftHybridRecommender(softness = 2.0).recommend(emptySet(), candidates.toSet())

            assertEquals(candidates[1], results[0])
            assertEquals(candidates[2], results[1])
            assertEquals(candidates[0], results[2])
        }
    }

    @Test
    fun `author recommendations order is preserved`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("AMAZING_RETURNS")), listOf("ANOTHER_GREAT_AUTHOR")),
            Book(listOf(Title("AMAZING_IN_SPACE")), listOf("ANOTHER_GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR"), rating = 2.0),
            Book(listOf(Title("book 1")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("book 2")), listOf("ANOTHER_GREAT_AUTHOR"), rating = 5.0),
            Book(listOf(Title("book 3")), listOf("ANOTHER_GREAT_AUTHOR")),
            Book(listOf(Title("book 4")), listOf("author"), rating = 3.0)
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[2], results[0])
            assertEquals(candidates[3], results[1])
            assertEquals(candidates[0], results[2])
            assertEquals(candidates[1], results[3])
            assertEquals(candidates[4], results[4])
        }
    }

    @Test
    fun `if rating is sufficiently low then softness should not falsely promote`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR"), rating = 5.0),
            Book(listOf(Title("book 1")), listOf("author"), rating = 4.0),
            Book(listOf(Title("book 2")), listOf("author"), rating = 2.0)
        )

        runBlocking {
            val results = SoftHybridRecommender(softness = 1.0).recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[0], results[0])
            assertEquals(candidates[1], results[1])
            assertEquals(candidates[2], results[2])
        }
    }

    @Test
    fun `duplicates are removed`() {
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("author"), rating = 5.0),
            Book(listOf(Title("book 1")), listOf("author"), rating = 4.0),
            Book(listOf(Title("book 1")), listOf("author"), rating = 4.0)
        )

        runBlocking { assertEquals(2, recommender.recommend(emptySet(), candidates.toSet()).size) }
    }

    @Test
    fun `collected books are filtered`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("book 1")), listOf("author"), rating = 4.0),
            Book(listOf(Title("book 2")), listOf("author"), rating = 2.0)
        )

        runBlocking { assertEquals(2, recommender.recommend(collection.toSet(), candidates.toSet()).size) }
    }

    @Test
    fun `large sets`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("AMAZING_RETURNS")), listOf("ANOTHER_GREAT_AUTHOR")),
            Book(listOf(Title("AMAZING_IN_SPACE")), listOf("ANOTHER_GREAT_AUTHOR")),
            Book(listOf(Title("AMAZING_IN_SPACE_2")), listOf("GREAT_AUTHOR_JR")),
            Book(listOf(Title("AMAZING_IN_SPACE_3")), listOf("GREAT_AUTHOR_JR")),
            Book(listOf(Title("AMAZING_BACK_IN_TIME")), listOf("GREAT_AUTHOR_JR")),
            Book(listOf(Title("OLD_BORING_STUFF")), listOf("GREAT_AUTHOR_SR")),
            Book(listOf(Title("OLD_BORING_STUFF")), listOf("GREAT_AUTHOR_SR")),
            Book(listOf(Title("JEZUS_WAT_KUT_ZEG")), listOf("GREAT_AUTHOR_SR")),
            Book(listOf(Title("CHEESE_1")), listOf("GREAT_AUTHOR_III")),
            Book(listOf(Title("CHEESE_2")), listOf("GREAT_AUTHOR_III")),
            Book(listOf(Title("CHEESE_3")), listOf("GREAT_AUTHOR_III")),
            Book(listOf(Title("CHEESE_4")), listOf("GREAT_AUTHOR_III")),
            Book(listOf(Title("CHEESE_5")), listOf("GREAT_AUTHOR_III")),
            Book(listOf(Title("BIOGRAPHY")), listOf("GARBAGE_AUTHOR")),
            Book(listOf(Title("BIOGRAPHY_REAWAKENS")), listOf("GARBAGE_AUTHOR_JR")),
            Book(listOf(Title("BIOGRAPHY_3000")), listOf("GARBAGE_AUTHOR_SR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR"), rating = 4.5),
            Book(listOf(Title("book 1")), listOf("GREAT_AUTHOR"), rating = 3.5),
            Book(listOf(Title("book 2")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("book 3")), listOf("GREAT_AUTHOR_JR")),
            Book(listOf(Title("book 4")), listOf("GREAT_AUTHOR_JR"), rating = 1.0),
            Book(listOf(Title("book 5")), listOf("GREAT_AUTHOR_SR"), rating = 2.2),
            Book(listOf(Title("book 6")), listOf("GREAT_AUTHOR_SR")),
            Book(listOf(Title("book 7")), listOf("GREAT_AUTHOR_III"), rating = 5.0),
            Book(listOf(Title("book 8")), listOf("GREAT_AUTHOR_III"), rating = 4.9),
            Book(listOf(Title("book 9")), listOf("GARBAGE_AUTHOR"), rating = 0.0),
            Book(listOf(Title("book 10")), listOf("GARBAGE_AUTHOR"), rating = 0.1),
            Book(listOf(Title("book 11")), listOf("GARBAGE_AUTHOR_SR"), rating = -0.1),
            Book(listOf(Title("book 12")), listOf("UNKNOWN"), rating = 3.0),
            Book(listOf(Title("book 13")), listOf("UNKNOWN"), rating = 2.0),
            Book(listOf(Title("book 14")), listOf("UNKNOWN"), rating = 1.0),
            Book(listOf(Title("book 15")), listOf("UNKNOWN")),
            Book(listOf(Title("book 16")), listOf("UNKNOWN")),
            Book(listOf(Title("book 17")), listOf("UNKNOWN")),
            Book(listOf(Title("book 17")), listOf("UNKNOWN"))
        )

        runBlocking {
            val results = SoftHybridRecommender(
                randomRecommender = RandomRecommender(Random(123)),
                softness = 2.0).recommend(collection.toSet(), candidates.toSet())

            assertEquals(18, results.size)
            assertEquals(candidates[7], results[0])
            assertEquals(candidates[8], results[1])
            assertEquals(candidates[3], results[2])
            assertEquals(candidates[6], results[3])
            assertEquals(candidates[0], results[4])
            assertEquals(candidates[1], results[5])
            assertEquals(candidates[5], results[6])
            assertEquals(candidates[2], results[7])
            assertEquals(candidates[12], results[8])
            assertEquals(candidates[4], results[9])
            assertEquals(candidates[10], results[10])
            assertEquals(candidates[13], results[11])
            assertEquals(candidates[9], results[12])
            assertEquals(candidates[11], results[13])
            assertEquals(candidates[14], results[14])
            assertEquals(candidates[16], results[15])
            assertEquals(candidates[17], results[16])
            assertEquals(candidates[15], results[17])

        }
    }
}
