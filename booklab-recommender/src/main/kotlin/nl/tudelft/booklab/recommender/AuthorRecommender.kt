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

class AuthorRecommender : Recommender {
    override fun recommend(collection: List<Book>, candidates: List<Book>): List<Pair<Book, Int>> {
        val authors = collection
            .map { it.authors }
            .fold(emptyList<String>()) { list, it -> list.plus(it) }
            .groupBy { it }
            .entries.sortedByDescending { it.value.size } // what if equal amount of books?
            .map { it.key }
        val maxScore = authors.size
        val scores = candidates
            .map { it to 0 }
            .toMap().toMutableMap()
        for (i in 0 until authors.size) {
            val selection = candidates.filter { it.authors.contains(authors[i]) }
            selection.forEach { scores.replace(it, scores.getValue(it) + maxScore - i) }
        }
        return scores
            .toList()
            .filter { !collection.contains(it.first) }
            .sortedByDescending { it.second }
    }
}
