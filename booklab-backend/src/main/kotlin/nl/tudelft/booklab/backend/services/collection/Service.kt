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

package nl.tudelft.booklab.backend.services.collection

import nl.tudelft.booklab.backend.services.catalogue.Book
import org.springframework.transaction.annotation.Transactional
import javax.validation.ConstraintViolationException

/**
 * A service for managing collection collections.
 */
class BookCollectionService(private val repository: BookCollectionRepository) {
    /**
     * Find a collection collection by its identifier.
     *
     * @param id The identifier of the collection.
     */
    @Transactional
    fun findById(id: Int): BookCollection? = repository.findById(id).orElse(null)

    /**
     * Determine whether a collection exists by its identifier.
     *
     * @param id The identifier of the collection to find.
     * @return `true` if the collection exists, `false` otherwise.
     */
    @Transactional
    fun existsById(id: Int): Boolean = repository.existsById(id)

    /**
     * Add the given books to the specified collection collection.
     *
     * @param collection The collection to add the books to.
     * @param books The books to add to the collection.
     * @return The collection collection that has been updated.
     */
    @Transactional
    @Throws(BookCollectionServiceException::class)
    fun addBooks(collection: BookCollection, books: Set<Book>): BookCollection {
        return setBooks(collection, collection.books + books)
    }

    /**
     * Delete the given books from the specified collection.
     *
     * @param collection The collection to add the books to.
     * @param books The books to remove from the collection.
     * @return The collection collection that has been updated.
     */
    @Transactional
    @Throws(BookCollectionServiceException::class)
    fun deleteBooks(collection: BookCollection, books: Set<Book>): BookCollection {
        return setBooks(collection, collection.books - books)
    }

    /**
     * Set the given books to the specified collection collection.
     *
     * @param collection The collection to set the books for.
     * @param books The books to add to the collection.
     * @return The collection collection that has been updated.
     */
    @Transactional
    @Throws(BookCollectionServiceException::class)
    fun setBooks(collection: BookCollection, books: Set<Book>): BookCollection {
        if (collection.user == null) {
            throw BookCollectionServiceException.InvalidInformationException("A valid user should be given.")
        }
        try {
            return repository.save(collection.copy(books = books))
        } catch (e: ConstraintViolationException) {
            throw BookCollectionServiceException.InvalidInformationException(
                e.message ?: "A constraint violation occurred"
            )
        }
    }

    /**
     * Register the given book collection to the specified repository.
     *
     * @param collection The collection to register.
     * @return The collection that has been registered.
     */
    @Transactional
    @Throws(BookCollectionServiceException::class)
    fun save(collection: BookCollection): BookCollection {
        val user = collection.user
        if (user == null) {
            throw BookCollectionServiceException.InvalidInformationException("A valid user should be given.")
        } else if (user.collections.any { it.name == collection.name }) {
            throw BookCollectionServiceException.BookCollectionAlreadyExistsException(
                "There exists already a collection with name '${collection.name}'."
            )
        }
        try {
            return repository.save(collection)
        } catch (e: ConstraintViolationException) {
            throw BookCollectionServiceException.InvalidInformationException(
                e.message ?: "A constraint violation occurred"
            )
        }
    }

    /**
     * Delete the given book collection from the underlying repository.
     *
     * @param collection The collection to delete.
     */
    @Transactional
    @Throws(BookCollectionServiceException::class)
    fun delete(collection: BookCollection) {
        repository.deleteById(collection.id)
    }
}

/**
 * An exception that is possibly thrown by a method of the [BookCollectionService] class.
 */
sealed class BookCollectionServiceException(description: String) : Exception(description) {
    /**
     * The given information about the repository was invalid.
     */
    class InvalidInformationException(description: String) : BookCollectionServiceException(description)

    /**
     * A collection with that name already exists.
     */
    class BookCollectionAlreadyExistsException(description: String) : BookCollectionServiceException(description)
}
