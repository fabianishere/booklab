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

package nl.tudelft.booklab.recommender.rating.goodreads

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.experimental.io.jvm.javaio.toInputStream
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.recommender.Recommender

/**
 * a [Recommender] that recommends solely based on the ratings from GoodReads
 * [GoodreadsRatingRecommender] implements the [Recommender] interface
 *
 * @property client the HTTP client used to connect with the Goodreads
 * database
 * @property key the Goodreads API key
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class GoodreadsRatingRecommender(
    private val client: HttpClient = HttpClient(Apache),
    private val key: String
) : Recommender {
    private val parser = GoodreadsParser()

    override suspend fun recommend(collection: Set<Book>, candidates: Set<Book>): List<Book> {
        val response = client.call {
            url(createUrl(candidates
                .map { it.ids }
                .fold(emptyList()) { list, it -> list.plus(it) }))
            method = HttpMethod.Get
        }.response
        if (response.status.value != HttpStatusCode.OK.value) { return emptyList() } // no candidates were found
        val ratings = parser.parse(response.content.toInputStream())
        val map = candidates
            .filter { !collection.contains(it) }
            .filter { ratings.contains(it.ids) }
            .map { it to 0.0 }
            .toMap().toMutableMap()
        map.forEach {
            if (ratings.contains(it.key.ids)) {
                map.replace(it.key, ratings.get(it.key.ids).rating.toDouble())
            }
        }
        return map
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }
    }

    /**
     * creates a url that queries the ratings of a list of books bases
     * on their ISBN numbers
     *
     * @param isbns the list of ISBN numbers to be queried
     * @return the string represented url
     */
    private fun createUrl(isbns: List<String>): String {
        return "https://www.goodreads.com/book/review_counts.json?key=$key&isbns=" +
            "${isbns.joinToString(",")}"
    }

    /**
     * a extension method used to check whether a ISBN number
     * is present in the [Results]
     *
     * @param isbns the list of ISBN numbers to check. if 1 of these
     * ISBN numbers is present in the ratings the method will return true
     * @return true if a ISBN is present else returns false
     */
    private fun Results.contains(isbns: List<String>): Boolean {
        return ratings
            .map { it.isbn10 }
            .plus( ratings.map { it.isbn13 })
            .intersect(isbns)
            .isNotEmpty()
    }

    /**
     * a extension method that retrieves the Rating associated with a
     * certain list of ISBN numbers
     *
     * @param isbns the list of ISBN numbers
     * @return a [Rating] associated with the ISBN numbers
     * @throws NoSuchElementException if no such element is found in the
     * [Results]
     */
    private fun Results.get(isbns: List<String>): Rating {
        return if (contains(isbns))
            ratings.filter { isbns.contains(it.isbn10) || isbns.contains(it.isbn13) }[0]
        else
            throw NoSuchElementException()
    }
}
