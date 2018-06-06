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

/**
 * a [Recommender] that uses multiple different types of recommenders
 * to give book recommendations. the [SoftHybridRecommender] implements
 * the [Recommender] interface. by default the recommender will recommend
 * authors present in the collection more than books with high ratings.
 * However when a book rating is sufficiently higher than the rating of the
 * book by that author this rule will be overridden.
 *
 * @property authorRecommender the recommender used to give recommendations
 * based on the authors
 * @property ratingRecommender the recommender used to give recommendations
 * based on the ratings of the books
 * @property randomRecommender the random recommender is used to make sure that
 * always recommendations are made. it simply suffles the candidates and filters
 * the already present books in the collection
 * @property softness when a rating is this amount larger than the rating of a
 * book of a recommended author then this rule is overridden.
 * example 1: book A is written by a recommended author and has rating of 3.0.
 * book B is only recommended based on the rating of 4.0 and the softness is 1.5.
 * this will result in that book A is recommended since the difference between the
 * ratings is to small.
 * example 2: book C is written by a recommended author and has rating of 2.0.
 * book D is only recommended based on the rating of 4.0 and the softness is 1.0.
 * however book D will be recommended more since it rating is more that 1.0 higher than
 * book book C
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
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
                fun Book.isRecommended(): Boolean = authorRecommendations.contains(this)
                fun Book.notRecommended(): Boolean = !this.isRecommended()
                fun Book.hasRating(): Boolean = this.rating != null
                fun Book.noRating(): Boolean = !this.hasRating()
                fun Book.index(): Int = authorRecommendations.indexOf(this)
                fun compareRating(book1: Book, book2: Book): Int = -compare(book1.rating!!, book2.rating!!)
                when {
                    o1.isRecommended() && o2.isRecommended() && !(o1.hasRating() && o2.hasRating()) ->
                        compare(o1.index(), o2.index())
                    o1.hasRating() && o1.notRecommended() && o2.hasRating() && o2.notRecommended() ->
                        compareRating(o1, o2)
                    o1.noRating() && o1.isRecommended() && o2.hasRating() && o2.notRecommended() ->
                        -1 // favor o1, not expected to happen often
                    o1.hasRating() && o1.notRecommended() && o2.noRating() && o2.isRecommended() ->
                        1 // favor o2, not expected to happen often
                    o1.hasRating() && o1.notRecommended() && o2.hasRating() && o2.isRecommended() ->
                        if (o1.rating!! - o2.rating!! < softness) 1 else compareRating(o1, o2)
                    o1.hasRating() && o1.isRecommended() && o2.hasRating() && o2.notRecommended() ->
                        if (o2.rating!! - o1.rating!! < softness) -1 else compareRating(o1, o2)
                    o1.hasRating() && o1.isRecommended() && o2.hasRating() && o2.isRecommended() ->
                        compareRating(o1, o2)
                    else -> throw IllegalStateException() // should be impossible to reach since this means that
                    // either o1 or o2 besides not having a rating also does not have a recommended author. this cannot
                    // be possible if the union of both those recommendations is taken.
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
