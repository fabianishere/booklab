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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthorRecommenderTest {
    private val recommender = AuthorRecommender()

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

        assertThat(results[0].first, equalTo(candidates[1]))
        assertThat(results[1].first, equalTo(candidates[2]))
        assertThat(results[2].first, equalTo(candidates[0]))
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

        assertThat(results.size, equalTo(2))
    }

    @Test
    fun `empty collection returns all the candidates with a score of zero`() {
        val candidates = listOf(
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 3"), listOf("isbn 6")),
            Book(listOf(Title("title 8", TitleType.MAIN)), listOf("author 2"), listOf("isbn 8")),
            Book(listOf(Title("title 9", TitleType.MAIN)), listOf("author 3"), listOf("isbn 9"))
        )

        val results = recommender.recommend(emptyList(), candidates)

        assertThat(results.size, equalTo(3))
        results.forEach { assertThat(it.second, equalTo(0)) }
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
