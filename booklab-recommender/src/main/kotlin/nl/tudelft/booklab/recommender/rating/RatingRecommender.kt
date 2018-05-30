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

package nl.tudelft.booklab.recommender.rating

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.experimental.io.jvm.javaio.toInputStream
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.recommender.RecommendException
import nl.tudelft.booklab.recommender.Recommender

/**
 * a [Recommender] that recommends solely based on the ratings from GoodReads
 * [RatingRecommender] implements the [Recommender] interface
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class RatingRecommender(
    private val client: HttpClient = HttpClient(Apache)
) : Recommender {

    override suspend fun recommend(collection: List<Book>, candidates: List<Book>): List<Pair<Book, Double>> {
        val response = client.call {
            url(createUrl(candidates
                .map { it.ids }
                .fold(emptyList()) { list, it -> list.plus(it) } ))
            method = HttpMethod.Get
        }.response
        if (response.status.value != HttpStatusCode.OK.value) { throw RecommendException() } // none of the candidates were found
        val ratings = GoodreadsParser.parse(response.content.toInputStream())
        val map = candidates
            .filter { !collection.contains(it) }
            .map { it to 0.0 }
            .toMap().toMutableMap()
        map.forEach{
            if (ratings.contains(it.key.ids)) {
                map.replace(it.key, ratings.get(it.key.ids).rating.toDouble())
            }
        }
        return map
            .toList()
            .sortedByDescending { it.second }
            .map { it.first to it.second }
    }

    private fun createUrl(isbns: List<String>): String {
        return "https://www.goodreads.com/book/review_counts.json?key=hfYu6aQhW8g1iHbsapRFow&isbns=" +
            "${isbns.joinToString(",")}" // TODO remove key from source
    }
}
