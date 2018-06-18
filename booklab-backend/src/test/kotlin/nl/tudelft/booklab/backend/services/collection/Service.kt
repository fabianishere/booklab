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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import javax.validation.ConstraintViolationException
import kotlin.test.assertEquals

/**
 * Test suite for the [BookCollectionService] class.
 */
internal class BookCollectionServiceTest {
    /**
     * The [BookCollectionRepository] to use.
     */
    private lateinit var repository: BookCollectionRepository

    /**
     * The [BookCollectionService] to test.
     */
    private lateinit var service: BookCollectionService

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        repository = mock()
        service = BookCollectionService(repository)
    }

    @Test
    fun `findById returns book from repository`() {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(1, user, "test", emptySet())

        repository.stub {
            on { findById(eq(1)) } doReturn Optional.of(collection)
        }

        val res = service.findById(1)
        assertEquals(collection, res)
        assertEquals(user, res?.user)
        verify(repository, times(1)).findById(eq(1))
    }

    @Test
    fun `findById returns null on unknown book`() {
        repository.stub {
            on { findById(eq(1)) } doReturn Optional.empty<BookCollection>()
        }

        assertNull(service.findById(1))
        verify(repository, times(1)).findById(eq(1))
    }

    @Test
    fun `existsById returns true on existing id`() {
        repository.stub {
            on { existsById(eq(1)) } doReturn true
        }

        assertTrue(service.existsById(1))
        verify(repository, times(1)).existsById(eq(1))
    }

    @Test
    fun `existsById returns false on non-existent id`() {
        repository.stub {
            on { existsById(eq(1)) } doReturn false
        }

        assertFalse(service.existsById(1))
        verify(repository, times(1)).existsById(eq(1))
    }

    @Test
    fun `save returns saved collection`() {
        val user = User(1, "test", "", emptyList())
        val collection = BookCollection(1, user, "test", emptySet())

        repository.stub {
            on { save(any<BookCollection>()) } doAnswer { it.getArgument(0) }
        }

        assertEquals(collection, service.save(collection))
    }

    @Test
    fun `save fails on invalid user`() {
        val collection = BookCollection(1, null, "test", emptySet())

        val throwable = assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.save(collection)
        }
        assertEquals("A valid user should be given.", throwable.message)
    }

    @Test
    fun `save fails on duplicate name for user`() {
        val user = User(1, "test", "", listOf(BookCollection(2, null, "test", emptySet())))
        val collection = BookCollection(1, user, "test", emptySet())

        val throwable = assertThrows<BookCollectionServiceException.BookCollectionAlreadyExistsException> {
            service.save(collection)
        }
        assertEquals("There exists already a collection with name '${collection.name}'.", throwable.message)
    }

    @Test
    fun `save fails on duplicate name for user 2`() {
        val user = User(1, "test", "", listOf(BookCollection(3, null, "aap", emptySet()), BookCollection(2, null, "test", emptySet())))
        val collection = BookCollection(1, user, "test", emptySet())

        val throwable = assertThrows<BookCollectionServiceException.BookCollectionAlreadyExistsException> {
            service.save(collection)
        }
        assertEquals("There exists already a collection with name '${collection.name}'.", throwable.message)
    }

    @Test
    fun `save fails on constraint error`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", emptySet())

        repository.stub {
            on { save(eq(collection)) } doThrow ConstraintViolationException(null)
        }

        assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.save(collection)
        }
    }

    @Test
    fun `save fails on constraint error 2`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", emptySet())

        val exception = mock<ConstraintViolationException> {
            on { message } doReturn "test"
        }

        repository.stub {
            on { save(eq(collection)) } doThrow exception
        }

        assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.save(collection)
        }
    }

    @Test
    fun `setBooks replaces existing collection`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val replace = setOf(
            Book("3", mapOf(Identifier.INTERNAL to "3"), "", "", emptyList())
        )

        repository.stub {
            on { save(any<BookCollection>()) } doAnswer { it.getArgument(0) }
        }

        assertEquals(replace, service.setBooks(collection, replace).books)
    }

    @Test
    fun `setBooks fails on constraint error`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val replace = setOf(
            Book("3", mapOf(Identifier.INTERNAL to "3"), "", "", emptyList())
        )

        repository.stub {
            on { save(any<BookCollection>()) } doThrow ConstraintViolationException(null)
        }

        assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.setBooks(collection, replace)
        }
    }

    @Test
    fun `setBooks fails on constraint error 2`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val replace = setOf(
            Book("3", mapOf(Identifier.INTERNAL to "3"), "", "", emptyList())
        )

        val exception = mock<ConstraintViolationException> {
            on { message } doReturn "test"
        }

        repository.stub {
            on { save(any<BookCollection>()) } doThrow exception
        }

        assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.setBooks(collection, replace)
        }
    }

    @Test
    fun `setBooks fails on invalid user`() {
        val collection = BookCollection(1, null, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val replace = setOf(
            Book("3", mapOf(Identifier.INTERNAL to "3"), "", "", emptyList())
        )

        assertThrows<BookCollectionServiceException.InvalidInformationException> {
            service.setBooks(collection, replace)
        }
    }

    @Test
    fun `addBooks adds books to a collection`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val add = setOf(
            Book("3", mapOf(Identifier.INTERNAL to "3"), "", "", emptyList())
        )

        repository.stub {
            on { save(any<BookCollection>()) } doAnswer { it.getArgument(0) }
        }

        assertEquals(collection.books + add, service.addBooks(collection, add).books)
    }

    @Test
    fun `deleteBooks deletes books from a collection`() {
        val user = User(1, "test", "")
        val collection = BookCollection(1, user, "test", setOf(
            Book("1", mapOf(Identifier.INTERNAL to "1"), "", "", emptyList()),
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        ))

        val delete = setOf(
            Book("2", mapOf(Identifier.INTERNAL to "2"), "", "", emptyList())
        )

        repository.stub {
            on { save(any<BookCollection>()) } doAnswer { it.getArgument(0) }
        }

        assertEquals(collection.books - delete, service.deleteBooks(collection, delete).books)
    }
}
