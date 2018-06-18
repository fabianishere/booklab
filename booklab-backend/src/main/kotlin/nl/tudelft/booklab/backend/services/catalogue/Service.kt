/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.booklab.backend.services.catalogue

import nl.tudelft.booklab.backend.services.user.UserServiceException
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Identifier
import org.springframework.transaction.annotation.Transactional
import javax.validation.ConstraintViolationException
import nl.tudelft.booklab.catalogue.Book as AbstractBook

/**
 * A service for managing books in the catalogue.
 */
class CatalogueService(
    private val books: CatalogueRepository,
    private val client: CatalogueClient
) {
    /**
     * Find a [Book] by its ISBN identifier or look it up in the catalogue.
     *
     * Internally, this method will do a lookup in the database and query an external catalogue if it is not found in
     * the database.
     *
     * @param id The identifier to do the lookup with.
     * @return The book that has been found.
     */
    @Transactional
    suspend fun findById(id: String): Book? {
        return books.findByIndustryId(id) ?: client.find(id)?.let {
            try {
                save(map(it))
            } catch (e: CatalogueServiceException) {
                null
            }
        }
    }

    /**
     * Query the catalogue with the given keywords.
     *
     * At the moment, we use the external catalogue for querying with keywords as opposed to our own database. In the
     * future, we should be able to move away from simply by changing the implementation of the methods.
     *
     * @param query The query to search in the catalogue with.
     * @param max The amount of results to return at maximum.
     * @return A collection of books related to the keywords ordered by relevance.
     */
    @Transactional
    suspend fun query(query: String, max: Int = 5): List<Book> {
        return client.query(query, max).mapNotNull {
            try {
                save(map(it))
            } catch (e: CatalogueServiceException) {
                null
            }
        }
    }

    /**
     * Query the catalogue for the book with the given title and author.
     *
     * @param title The title of the book.
     * @param author The author of the book to look for.
     * @param max The amount of results to return at maximum.
     * @return A collection of books associated with the title and author.
     */
    @Transactional
    suspend fun query(title: String, author: String, max: Int = 5): List<Book> {
        return client.query(title, author, max).mapNotNull {
            try {
                save(map(it))
            } catch (e: CatalogueServiceException) {
                null
            }
        }
    }

    /**
     * Register the given [Book] to the specified repository.
     *
     * @param book The collection to register.
     * @return The book hat has been registered.
     */
    @Transactional
    @Throws(UserServiceException::class)
    fun save(book: Book): Book {
        try {
            return books.save(book)
        } catch (e: ConstraintViolationException) {
            throw CatalogueServiceException.InvalidInformationException(
                e.message ?: "A constraint violation occurred"
            )
        }
    }

    /**
     * Map an external [AbstractBook] into a persistent [Book].
     */
    private fun map(book: AbstractBook): Book {
        val identifier = book.identifiers[Identifier.ISBN_13]
            ?: book.identifiers[Identifier.ISBN_10]
            ?: book.identifiers[Identifier.INTERNAL]
            ?: throw CatalogueServiceException.InvalidInformationException("No valid identifier for book")
        return Book(
            identifier,
            book.identifiers,
            book.title,
            book.subtitle,
            book.authors,
            book.publisher,
            book.categories,
            book.publishedAt,
            book.description,
            book.language,
            book.ratings,
            book.images
        )
    }
}

/**
 * An exception that is possibly thrown by a method of the [CatalogueService] class.
 */
sealed class CatalogueServiceException(description: String) : Exception(description) {
    /**
     * The given information about the [Book] was invalid.
     */
    class InvalidInformationException(description: String) : CatalogueServiceException(description)
}
