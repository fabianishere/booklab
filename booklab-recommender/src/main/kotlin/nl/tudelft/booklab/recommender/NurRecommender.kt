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

/**
 * A singleton object that is able to recommend a selection of books based
 * on the nur (Nederlandstalige Uniforme Rubrieksindeling) code that is stored
 * in the database
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class NurRecommender : Recommender {
    override fun recommend(collection: List<Book>, candidates: List<Book>): List<Pair<Book, Int>> {
        val nurs = collection
            .map { it.nur }
            .groupBy { it }
            .entries.sortedByDescending { it.value.size }
            .map { it.key }
            .filter { it != null }
        val maxScore = nurs.size // any other ideas?
        val scores = candidates
            .filter { !collection.contains(it) }
            .map { it to 0 }
            .toMap().toMutableMap()
        for (i in 0 until nurs.size) {
            val selection = candidates.filter { it.nur == nurs[i] }
            selection.forEach { scores.replace(it, scores.getValue(it) + maxScore - i) }
        }
        return scores.toList().sortedByDescending { it.second }
    }
}
