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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StrictHybridRecommenderTest {
    private lateinit var recommender: StrictHybridRecommender

    @BeforeEach
    fun setup() {
        recommender = StrictHybridRecommender()
    }

    @Test
    fun `recommended authors are always recommended more than ratings`() {
        val collection = listOf(
            Book(listOf(Title("AMAZING")), listOf("GREAT_AUTHOR"))
        )
        val candidates = listOf(
            Book(listOf(Title("book 0")), listOf("GREAT_AUTHOR")),
            Book(listOf(Title("book 1")), listOf("author"), rating = 5.0),
            Book(listOf(Title("book 2")), listOf("author"), rating = 3.0)
        )

        runBlocking {
            val results = recommender.recommend(collection.toSet(), candidates.toSet())

            Assertions.assertEquals(candidates[0], results[0])
            Assertions.assertEquals(candidates[1], results[1])
            Assertions.assertEquals(candidates[2], results[2])
        }
    }
}
