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

import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.catalogue.TitleType
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
            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author 1"), listOf("isbn 7")),
            Book(listOf(Title("title 8", TitleType.MAIN)), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9", TitleType.MAIN)), listOf("author 3"), listOf("isbn 9"))
        )

        val results = recommender.recommend(collection, candidates)

        assertEquals(candidates[1], results[0].first)
        assertEquals(candidates[0], results[1].first)
        assertEquals(candidates[2], results[2].first)
    }

    @Test
    fun `collected books are discarded from candidates`() {
        val collection = listOf(
            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6")),
            Book(listOf(Title("title 8", TitleType.MAIN)), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9", TitleType.MAIN)), listOf("author 3"), listOf("isbn 9"))
        )

        val results = recommender.recommend(collection, candidates)

        assertEquals(2, results.size)
    }

    @Test
    fun `empty collection returns all the candidates with a score of zero`() {
        val candidates = listOf(
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6")),
            Book(listOf(Title("title 8", TitleType.MAIN)), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9", TitleType.MAIN)), listOf("author 3"), listOf("isbn 9"))
        )

        val results = recommender.recommend(emptyList(), candidates)

        assertEquals(3, results.size)
        results.forEach { assertEquals(0, it.second) }
    }

    @Test
    fun `no candidates returns a empty list`() {
        val collection = listOf(
            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6"))
        )

        val results = recommender.recommend(collection, emptyList())

        assertTrue(results.isEmpty())
    }
}
