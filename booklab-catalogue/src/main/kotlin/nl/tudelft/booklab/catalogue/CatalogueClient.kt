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

package nl.tudelft.booklab.catalogue

/**
 * a interface that standardizes a book catalogue client that is
 * used to query lists of [Book]
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
interface CatalogueClient {

    /**
     * queries the catalogue for a list of [Book]s based on keywords.
     * fuzzy search is used for matching the keywords
     *
     * @param keywords is a space separated string of keywords
     * @param max the maximum number of results to be expected
     * it is not assured that this number of records is returned
     * but never more than this amount
     * @return a list of [Book]s matching the keywords sorted by relevance
     */
    suspend fun query(keywords: String, max: Int): List<Book>

    /**
     * queries the catalogue for a list of [Book]s based on the
     * book title and the authors name. fuzzy search is used to
     * match the title and the author
     *
     * @param title keywords from the the title of the book
     * @param author keywords matching the authors name
     * @param max the maximum number of results to be expected
     * it is not assured that this number of records is returned
     * but never more than this amount
     * @return a list of [Book]s matching the keywords sorted by relevance
     */
    suspend fun query(title: String, author: String, max: Int): List<Book>
}
