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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import nl.tudelft.booklab.catalogue.sru.Book
import nl.tudelft.booklab.catalogue.sru.Title
import nl.tudelft.booklab.catalogue.sru.TitleType

class RecommenderTest {
//    @Test
//    fun `default test`() {
//        val collection = listOf(
//            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author 0"), listOf("isbn"), 100),
//            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author 0"), listOf("isbn"), 200),
//            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author 0"), listOf("isbn"), 200),
//            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author 1"), listOf("isbn"), 200),
//            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author 1"), listOf("isbn"), 200),
//            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author 1"), listOf("isbn"), 300),
//            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author 1"), listOf("isbn"), 300),
//            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author 1"), listOf("isbn"), 300)
//        )
//        val candidates = listOf(
//            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("recommend 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//
//        val selection = Recommender.recommend(collection, candidates)
//
//        assertThat(selection.size, equalTo(1))
//        assertThat(selection, equalTo(200))
//    }
//
//    @Test
//    fun `multiple results`() {
//        val collection = listOf(
//            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//        val candidates = listOf(
//            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("recommend 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200)
//        )
//
//        assertThat(Recommender.recommend(collection, candidates).size, equalTo(3))
//    }
//
//    @Test
//    fun `no recommendations possible`() {
//        val collection = listOf(
//            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//        val candidates = listOf(
//            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 400)
//        )
//
//        assertThat(Recommender.recommend(collection, candidates).isEmpty(), equalTo(true))
//    }
//
//    @Test
//    fun `second best pick`() {
//        val collection = listOf(
//            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//        val candidates = listOf(
//            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("recommend 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//
//        val selection = Recommender.recommend(collection, candidates)
//
//        assertThat(selection[0].nur, equalTo(300))
//    }
//
//    @Test
//    fun `exclude already collected books`() {
//        val collection = listOf(
//            Book(listOf(Title("collected", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100)
//        )
//        val candidates = listOf(
//            Book(listOf(Title("collected", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100)
//        )
//
//        assertThat(Recommender.recommend(collection, candidates).isEmpty(), equalTo(true))
//    }
//
//    @Test
//    fun `no candidates return no recommendations`() {
//        val collection = listOf(
//            Book(listOf(Title("title 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("title 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 3", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 4", TitleType.MAIN)), listOf("author"), listOf("isbn"), 200),
//            Book(listOf(Title("title 5", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 6", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("title 7", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//
//        assertThat(Recommender.recommend(collection, emptyList()).isEmpty(), equalTo(true))
//    }
//
//    @Test
//    fun `no collection returns no recommendations`() {
//        val candidates = listOf(
//            Book(listOf(Title("recommend 0", TitleType.MAIN)), listOf("author"), listOf("isbn"), 100),
//            Book(listOf(Title("recommend 1", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300),
//            Book(listOf(Title("recommend 2", TitleType.MAIN)), listOf("author"), listOf("isbn"), 300)
//        )
//
//        assertThat(Recommender.recommend(emptyList(), candidates).isEmpty(), equalTo(true))
//    }
}
