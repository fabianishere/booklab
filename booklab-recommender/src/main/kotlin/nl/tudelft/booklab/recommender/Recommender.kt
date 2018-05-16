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
import kotlin.math.max

/**
 * A singleton object that is able to recommend a selection of books based
 * on the nur (Nederlandstalige Uniforme Rubrieksindeling) code that is stored
 * in the database
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
object Recommender {
    private fun tagRecommend(collection: List<Book>, scores: MutableMap<Book, Int>): MutableMap<Book, Int> {
        val tags = collection
            .map { it.nur }
            .groupBy { it }
            .entries.sortedByDescending { it.value.size }
            .map { it.key }
            .filter { it != -1 }
        val maxScore = tags.size
        val candidates = scores.map { it.key }.toList()
        for (i in 0 until tags.size) {
            val selection = candidates.filter { it.nur == tags[i] }
            selection.forEach { scores.replace(it, scores.getValue(it) + maxScore - i) }
        }
        return scores
    }

    private fun authorRecommend(collection: List<Book>, scores: MutableMap<Book, Int>): MutableMap<Book, Int> {
        val authors = collection
            .map { it.authors }
            .reduce{ acc, it -> acc.plus(it) }
            .groupBy { it }
            .entries.sortedByDescending { it.value.size }
            .map { it.key }
        val maxScore = authors.size
        val candidates = scores.map { it.key }.toList()
        for (i in 0 until authors.size) {
            val selection = candidates.filter { it.authors.contains(authors[i]) }
            selection.forEach { scores.replace(it, scores.getValue(it) + maxScore - i) }
        }
        return scores
    }

    fun recommend(collection: List<Book>, candidates: List<Book>, max: Int = 5): List<Book> {
        return getRecommendScores(collection, candidates, max)
            .entries.sortedByDescending { it.value }
            .map { it.key }
            .subList(0, max)
    }

    fun getRecommendScores(collection: List<Book>, candidates: List<Book>, max: Int = 5): Map<Book, Int> {
        return authorRecommend(collection, tagRecommend(collection, candidates
            .filter { !collection.contains(it) }
            .map { it to 0 }
            .toMap().toMutableMap())).toMap()
    }
}
