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

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.config
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.experimental.io.jvm.javaio.toInputStream
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.sru.SruParser

/**
 * a [Recommender] that recommends solely based on the ratings from GoodReads
 * [RatingRecommender] implements the [Recommender] interface
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class RatingRecommender(
    private val client: HttpClient = HttpClient(io.ktor.client.engine.apache.Apache)
) : Recommender {

    override fun recommend(collection: List<Book>, candidates: List<Book>): List<Pair<Book, Int>> {
        val stream = client.call {
            url(createUrl(candidates
                .map { it.ids }
                .fold(emptyList<String>()) { list, it -> list.plus(it) } ))
            method = HttpMethod.Get
        }.response.content.toInputStream()
        return
    }

    private fun createUrl(isbns: List<String>): String {
        return "https://www.goodreads.com/book/review_counts.json?key=hfYu6aQhW8g1iHbsapRFow&isbns=" +
            "${isbns.joinToString(",")}"
    }
}
