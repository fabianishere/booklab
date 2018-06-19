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

/**
 * a [Recommender] that uses multiple different types of recommenders
 * to give book recommendations. the [StrictHybridRecommender] implements
 * the [Recommender] interface. by default the recommender will recommend
 * authors present in the collection more than books with high ratings.
 * the [SoftHybridRecommender] would overrule this rule by using a softness
 * factor. a higher softness means a stricter recommender. the
 * [StrictHybridRecommender] has "infinite" softness.
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class StrictHybridRecommender(
    authorRecommender: Recommender = AuthorRecommender(),
    ratingRecommender: Recommender = GoogleBooksRatingRecommender(),
    randomRecommender: Recommender = RandomRecommender()
) : Recommender {

    private val softHybridRecommender = SoftHybridRecommender(
        authorRecommender,
        ratingRecommender,
        randomRecommender,
        Double.MAX_VALUE
    )

    override suspend fun recommend(collection: Set<Book>, candidates: Set<Book>): List<Book> {
        return softHybridRecommender.recommend(collection, candidates)
    }
}
