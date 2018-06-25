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
import com.google.api.services.books.model.Volume
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.catalogue.Ratings
import java.net.URL
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
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
    /**
     * The [DateTimeFormatter] to use to parse the date.
     */
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy[-MM[-dd]]")

    /**
     * The [TemporalQuery]s to use to parse the different formats.
     */
    private val queries = arrayOf(TemporalQuery { LocalDate.from(it) },
        TemporalQuery { YearMonth.from(it) },
        TemporalQuery { Year.from(it) })

    override suspend fun find(isbn: String): Book? = query("isbn:$isbn", 1).firstOrNull()

    override suspend fun query(keywords: String, max: Int): List<Book> {
        // Google Books fails on empty query (#121)
        if (keywords.isBlank()) {
            return emptyList()
        }

        val response = catalogue.volumes().list(keywords).setMaxResults(min(40, max).toLong()).execute()

        // BUG (Google Books API): The items field is null when no items could be found, return null instead
        if (response.totalItems == 0) {
            return emptyList()
        }
        return response.items.mapNotNull { map(it) }
    }

    override suspend fun query(title: String, author: String, max: Int): List<Book> {
        return query("intitle:$title+inauthor:$author", max)
    }

    /**
     * Map the given [Volume] to a [Book] instance.
     *
     * @param volume The volume to map.
     * @return The book that has been mapped or `null` if the book could not be converted.
     */
    private fun map(volume: Volume): Book? {
        val info = volume.volumeInfo
        val identifiers = info.industryIdentifiers
            ?.mapNotNull {
                val type = try { Identifier.valueOf(it.type) } catch (e: IllegalArgumentException) { null }
                val value = it.identifier
                type?.let { it to value }
            }
            ?.takeIf { it.isNotEmpty() }
            ?.plus(Identifier.INTERNAL to volume.id)
            ?.toMap() ?: return null
        val categories = info.categories?.toSet() ?: emptySet()
        val publishedAt = info.publishedDate?.let {
            try {
                formatter.parseBest(it, *queries)
            } catch (e: DateTimeParseException) {
                null
            }
        }
        val ratings = let {
            if (info.ratingsCount != null && info.averageRating != null)
                Ratings(info.averageRating, info.ratingsCount)
            else
                null
        }
        val images = info.imageLinks?.mapValues { URL(it.value as String) } ?: emptyMap()
        return GoogleBooksBook(
            identifiers,
            info.title,
            info.subtitle,
            info.authors ?: emptyList(),
            info.publisher,
            categories,
            publishedAt,
            info.description,
            info.language,
            ratings,
            images
        )
    }
}

/**
 * A [Book] implementation for Google Books.
 */
class GoogleBooksBook(
    override val identifiers: Map<Identifier, String>,
    override val title: String,
    override val subtitle: String?,
    override val authors: List<String>,
    override val publisher: String?,
    override val categories: Set<String>,
    override val publishedAt: TemporalAccessor?,
    override val description: String?,
    override val language: String?,
    override val ratings: Ratings?,
    override val images: Map<String, URL>
) : Book()
