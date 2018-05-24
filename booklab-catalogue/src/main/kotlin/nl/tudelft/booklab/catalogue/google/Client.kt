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

package nl.tudelft.booklab.catalogue.google

import com.google.api.services.books.Books
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.catalogue.TitleType
import kotlin.math.min

/**
 * A [CatalogueClient] that uses the Google Books API to query lists of [Book]s.
 * it implements the [CatalogueClient] interface
 *
 * @property catalogue where the books are queried from. it defaults to the entire
 * Google Books database
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class GoogleCatalogueClient(private val catalogue: Books) : CatalogueClient {

    override suspend fun query(keywords: String, max: Int): List<Book> {
        val response = catalogue.volumes().list(keywords).setMaxResults(min(40, max).toLong()).execute()

        // BUG: The items field is null when no items could be found, return null instead
        if (response.totalItems == 0) {
            return emptyList()
        }
        return response.items
            .asSequence()
            .filter { it.volumeInfo.industryIdentifiers != null }
            .filter { it.volumeInfo.authors != null }
            .filter { it.volumeInfo.title != null }
            .map {
                Book(
                    listOfNotNull(
                        Title(it.volumeInfo.title, TitleType.MAIN),
                        it.volumeInfo.subtitle?.let { Title(it, TitleType.SUB) }
                    ),
                    it.volumeInfo.authors,
                    it.volumeInfo.industryIdentifiers
                        .filter { it.type == "ISBN_10" || it.type == "ISBN_13" }
                        .map { it.identifier })
        }.toList()
    }

    override suspend fun query(title: String, author: String, max: Int): List<Book> {
        return query("intitle:$title+inauthor:$author", max)
    }
}
