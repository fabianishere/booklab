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
import java.util.*

class RandomRecommender(private val random: Random = Random()) : Recommender {
    override fun recommend(collection: List<Book>, candidates: List<Book>): List<Pair<Book, Int>> {
        return candidates
            .filter { !collection.contains(it) }
            .shuffled(random)
            .map { it to 0 }
    }
}
