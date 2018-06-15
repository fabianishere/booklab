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
            TestBook(mapOf(Identifier.INTERNAL to "123"), "title 0", listOf("GREAT_AUTHOR"))
        )

        runBlocking { assertTrue(recommender.recommend(collection.toSet(), emptySet()).isEmpty()) }
    }

    @Test
    fun `no collection does not fail`() {
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "123"), "title 0", listOf("author")),
            TestBook(mapOf(Identifier.INTERNAL to "234"), "title 1", listOf("author"))
        )

        runBlocking { assertEquals(2, recommender.recommend(emptySet(), candidates.toSet()).size) }
    }

    @Test
    fun `if both books have a rating and a recommended author favor the rating`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "123"), "AMAZING", listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "124"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(4.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "233"), "book 1", listOf("GREAT_AUTHOR"), ratings = Ratings(5.0, 1))
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
            TestBook(mapOf(Identifier.INTERNAL to "123"), "AMAZING", listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "125"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(2.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "233"), "book 1", listOf("author"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "234"), "book 2", listOf("author"), ratings = Ratings(3.0, 1))
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
            TestBook(mapOf(Identifier.INTERNAL to "234"), "book 0", listOf("author"), ratings = Ratings(2.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "235"), "book 1", listOf("author"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "237"), "book 2", listOf("author"), ratings = Ratings(3.0, 1))
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
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "AMAZING_RETURNS", listOf("ANOTHER_GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "AMAZING_IN_SPACE", listOf("ANOTHER_GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "4"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(2.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "5"), "book 1", listOf("GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "6"), "book 2", listOf("ANOTHER_GREAT_AUTHOR"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "7"), "book 3", listOf("ANOTHER_GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "8"), "book 4", listOf("author"), ratings = Ratings(3.0, 1))
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
    fun `random recommendations are lowest recommended`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "2"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(2.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "book 1", listOf("author")),
            TestBook(mapOf(Identifier.INTERNAL to "4"), "book 2", listOf("author")),
            TestBook(mapOf(Identifier.INTERNAL to "5"), "book 3", listOf("author"))
        )

        runBlocking {
            val results = SoftHybridRecommender(randomRecommender = RandomRecommender(Random(123)), softness = 2.0)
                .recommend(collection.toSet(), candidates.toSet())

            assertEquals(candidates[0], results[0])
            assertEquals(candidates[1], results[1])
            assertEquals(candidates[3], results[2])
            assertEquals(candidates[2], results[3])
        }
    }

    @Test
    fun `if rating is sufficiently low then softness should not falsely promote`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "2"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "book 1", listOf("author"), ratings = Ratings(4.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "4"), "book 1", listOf("author"), ratings = Ratings(2.0, 1))
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
            TestBook(mapOf(Identifier.INTERNAL to "1"), "book 0", listOf("author"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "book 1", listOf("author"), ratings = Ratings(4.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "book 1", listOf("author"), ratings = Ratings(4.0, 1))
        )

        runBlocking { assertEquals(2, recommender.recommend(emptySet(), candidates.toSet()).size) }
    }

    @Test
    fun `collected books are filtered`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "book 1", listOf("author"), ratings = Ratings(4.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "book 2", listOf("author"), ratings = Ratings(2.0, 1))
        )

        runBlocking { assertEquals(2, recommender.recommend(collection.toSet(), candidates.toSet()).size) }
    }

    @Test
    fun `large sets`() {
        val collection = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "1"), "AMAZING", listOf("GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "2"), "AMAZING_RETURNS", listOf("ANOTHER_GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "3"), "AMAZING_IN_SPACE", listOf("ANOTHER_GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "4"), "AMAZING_IN_SPACE_2", listOf("GREAT_AUTHOR_JR")),
            TestBook(mapOf(Identifier.INTERNAL to "5"), "AMAZING_IN_SPACE_3", listOf("GREAT_AUTHOR_JR")),
            TestBook(mapOf(Identifier.INTERNAL to "5"), "AMAZING_BACK_IN_TIME", listOf("GREAT_AUTHOR_JR")),
            TestBook(mapOf(Identifier.INTERNAL to "6"), "OLD_BORING_STUFF", listOf("GREAT_AUTHOR_SR")),
            TestBook(mapOf(Identifier.INTERNAL to "7"), "OLD_BORING_STUFF", listOf("GREAT_AUTHOR_SR")),
            TestBook(mapOf(Identifier.INTERNAL to "8"), "JEZUS_WAT_KUT_ZEG", listOf("GREAT_AUTHOR_SR")),
            TestBook(mapOf(Identifier.INTERNAL to "9"), "CHEESE_1", listOf("GREAT_AUTHOR_III")),
            TestBook(mapOf(Identifier.INTERNAL to "10"), "CHEESE_2", listOf("GREAT_AUTHOR_III")),
            TestBook(mapOf(Identifier.INTERNAL to "11"), "CHEESE_3", listOf("GREAT_AUTHOR_III")),
            TestBook(mapOf(Identifier.INTERNAL to "12"), "CHEESE_4", listOf("GREAT_AUTHOR_III")),
            TestBook(mapOf(Identifier.INTERNAL to "13"), "CHEESE_5", listOf("GREAT_AUTHOR_III")),
            TestBook(mapOf(Identifier.INTERNAL to "14"), "BIOGRAPHY", listOf("GARBAGE_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "15"), "BIOGRAPHY_REAWAKENS", listOf("GARBAGE_AUTHOR_JR")),
            TestBook(mapOf(Identifier.INTERNAL to "16"), "BIOGRAPHY_3000", listOf("GARBAGE_AUTHOR_SR"))
        )
        val candidates = listOf(
            TestBook(mapOf(Identifier.INTERNAL to "17"), "book 0", listOf("GREAT_AUTHOR"), ratings = Ratings(4.5, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "18"), "book 1", listOf("GREAT_AUTHOR"), ratings = Ratings(3.5, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "19"), "book 2", listOf("GREAT_AUTHOR")),
            TestBook(mapOf(Identifier.INTERNAL to "20"), "book 3", listOf("GREAT_AUTHOR_JR")),
            TestBook(mapOf(Identifier.INTERNAL to "21"), "book 4", listOf("GREAT_AUTHOR_JR"), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "22"), "book 5", listOf("GREAT_AUTHOR_SR"), ratings = Ratings(2.2, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "23"), "book 6", listOf("GREAT_AUTHOR_SR")),
            TestBook(mapOf(Identifier.INTERNAL to "24"), "book 7", listOf("GREAT_AUTHOR_III"), ratings = Ratings(5.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "25"), "book 8", listOf("GREAT_AUTHOR_III"), ratings = Ratings(4.9, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "26"), "book 9", listOf("GARBAGE_AUTHOR"), ratings = Ratings(0.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "27"), "book 10", listOf("GARBAGE_AUTHOR"), ratings = Ratings(0.1, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "28"), "book 11", listOf("GARBAGE_AUTHOR_SR"), ratings = Ratings(-0.1, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "29"), "book 12", listOf("UNKNOWN"), ratings = Ratings(3.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "30"), "book 13", listOf("UNKNOWN"), ratings = Ratings(2.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "31"), "book 14", listOf("UNKNOWN"), ratings = Ratings(1.0, 1)),
            TestBook(mapOf(Identifier.INTERNAL to "32"), "book 15", listOf("UNKNOWN")),
            TestBook(mapOf(Identifier.INTERNAL to "33"), "book 16", listOf("UNKNOWN")),
            TestBook(mapOf(Identifier.INTERNAL to "34"), "book 17", listOf("UNKNOWN")),
            TestBook(mapOf(Identifier.INTERNAL to "34"), "book 17", listOf("UNKNOWN"))
        )

        runBlocking {
            val results = SoftHybridRecommender(
                randomRecommender = RandomRecommender(Random(123)),
                softness = 2.0).recommend(collection.toSet(), candidates.toSet())

            results.forEach { println(it) }

            assertEquals(18, results.size)
            assertEquals(candidates[7], results[0])
            assertEquals(candidates[8], results[1])
            assertEquals(candidates[6], results[2])
            assertEquals(candidates[3], results[3])
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
