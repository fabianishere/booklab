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

import nl.tudelft.booklab.catalogue.Book

/**
 * an interface that standardizes the different types of recommenders
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
interface Recommender {
    /**
     * the default recommend function
     *
     * @param collection a list of [Book]s that represents the collection
     * of the user
     * @param candidates a list of [Book]s that represents the domain out
     * of which a recommendation needs to be made
     * @return a list of [Pair]s that of [Book]s and a accompanying score.
     * the list is sorted decreasingly and does not contain any books that
     * are already in the collection
     */
    suspend fun recommend(collection: Set<Book>, candidates: Set<Book>): List<Book>
}

class RecommendException : Exception()
