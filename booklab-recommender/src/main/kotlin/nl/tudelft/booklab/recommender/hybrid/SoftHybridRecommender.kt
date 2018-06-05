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

package nl.tudelft.booklab.recommender.hybrid

import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.recommender.Recommender
import nl.tudelft.booklab.recommender.author.AuthorRecommender
import nl.tudelft.booklab.recommender.random.RandomRecommender
import nl.tudelft.booklab.recommender.rating.google.GoogleBooksRatingRecommender
import java.lang.Double.compare
import java.lang.Integer.compare
import java.lang.Math.abs

class SoftHybridRecommender(
    private val authorRecommender: Recommender = AuthorRecommender(),
    private val ratingRecommender: Recommender = GoogleBooksRatingRecommender(),
    private val randomRecommender: Recommender = RandomRecommender(),
    private val softness: Double
) : Recommender {

    override suspend fun recommend(collection: Set<Book>, candidates: Set<Book>): List<Book> {
        val authorRecommendations = authorRecommender.recommend(collection, candidates)
        val ratingRecommendations = ratingRecommender.recommend(collection, candidates)
        val randomRecommendations = randomRecommender.recommend(collection, candidates)
        val mergedRecommendations = ratingRecommendations.union(authorRecommendations)
            .sortedWith(Comparator {
                o1, o2 ->
                    when {
                        authorRecommendations.contains(o1) == authorRecommendations.contains(o2)
                            || (o1.rating != null && o2.rating != null && abs(o1.rating!! - o2.rating!!) > softness) ->
                            -compare(o1.rating!!, o2.rating!!)
                        authorRecommendations.contains(o1) -> -1
                        else -> 1
                    }
            })
        return randomRecommendations
            .sortedWith(Comparator {
                o1, o2 ->
                    val index1 = mergedRecommendations.indexOf(o1)
                    val index2 = mergedRecommendations.indexOf(o2)
                    when {
                        index1 == -1 && index2 == -1 -> 0
                        index1 == -1 -> 1
                        index2 == -1 -> -1
                        else -> compare(index1, index2)
                    }
        })
    }
}
