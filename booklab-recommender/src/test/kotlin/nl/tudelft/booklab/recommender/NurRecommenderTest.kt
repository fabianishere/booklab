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

import nl.tudelft.booklab.catalogue.sru.Book
import nl.tudelft.booklab.catalogue.sru.Title
import nl.tudelft.booklab.catalogue.sru.TitleType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class NurRecommenderTest {
    @Test
    fun `default test`() {
        val collection = listOf(
            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author 0"), listOf("isbn 0"), 100),
            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author 0"), listOf("isbn 1"), 200),
            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author 0"), listOf("isbn 2"), 200),
            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author 1"), listOf("isbn 3"), 200),
            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author 1"), listOf("isbn 4"), 200),
            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author 1"), listOf("isbn 5"), 300),
            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 1"), listOf("isbn 6"), 300),
            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author 1"), listOf("isbn 7"), 300)
        )
        val candidates = listOf(
            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn 8"), 100),
            Book(listOf(Title("recommend 1", TitleType.MAIN)), listOf("author"), listOf("isbn 9"), 200),
            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn 10"), 300),
            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn 10"), 400)
        )

        val recommendations = NurRecommender().recommend(collection, candidates)
        recommendations.forEach { println(it) }

        assertThat(1, equalTo(2))
    }
}
