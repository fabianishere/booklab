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
import nl.tudelft.booklab.catalogue.TitleType
import org.junit.jupiter.api.Assertions.assertEquals
import nl.tudelft.booklab.recommender.author.AuthorRecommender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorRecommenderTest {
    private lateinit var recommender: Recommender

    @BeforeEach
    fun setUp() {
        recommender = AuthorRecommender()
    }

    @Test
    fun `default test`() {
        val collection = listOf(
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 7")), listOf("author 1"), listOf("isbn 7")),
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9")), listOf("author 3"), listOf("isbn 9"))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), candidates.toSet()) }

        assertEquals(candidates[1], results[0])
        assertEquals(candidates[2], results[1])
        assertEquals(candidates[0], results[2])
    }

    @Test
    fun `collected books are discarded from candidates`() {
        val collection = listOf(
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6")),
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9")), listOf("author 3"), listOf("isbn 9"))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), candidates.toSet()) }

        assertEquals(2, results.size)
    }

    @Test
    fun `empty collection returns no recommendations`() {
        val candidates = listOf(
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6")),
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9")), listOf("author 3"), listOf("isbn 9"))
        )

        val results = runBlocking { recommender.recommend(emptySet(), candidates.toSet()) }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `books with authors not in the collection are discarded`() {
        val collection = listOf(
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 7")), listOf("author 4"), listOf("isbn 7")),
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9")), listOf("author 3"), listOf("isbn 9"))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), candidates.toSet()) }

        assertEquals(2, results.size)
    }

    @Test
    fun `no candidates returns a empty list`() {
        val collection = listOf(
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6"))
        )

        val results = runBlocking { recommender.recommend(collection.toSet(), emptySet()) }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `remove duplicates from candidates`() {
        val collection = listOf(
            Book(listOf(Title("title 1")), listOf("author 1"), listOf("isbn 1")),
            Book(listOf(Title("title 2")), listOf("author 2"), listOf("isbn 2")),
            Book(listOf(Title("title 3")), listOf("author 2"), listOf("isbn 3")),
            Book(listOf(Title("title 4")), listOf("author 2"), listOf("isbn 4")),
            Book(listOf(Title("title 5")), listOf("author 3"), listOf("isbn 5")),
            Book(listOf(Title("title 6")), listOf("author 3"), listOf("isbn 6"))
        )
        val candidates = listOf(
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 8")), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9")), listOf("author 3"), listOf("isbn 9"))
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            assertEquals(2, results.size)
        }
    }
}
