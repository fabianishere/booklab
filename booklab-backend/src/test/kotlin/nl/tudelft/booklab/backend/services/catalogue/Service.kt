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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.catalogue.CatalogueClient
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.validation.ConstraintViolationException

/**
 * Test suite for the [CatalogueService] class.
 */
internal class CatalogueServiceTest {
    /**
     * The [CatalogueRepository] to use.
     */
    private lateinit var repository: CatalogueRepository

    /**
     * The external [CatalogueClient] to use.
     */
    private lateinit var client: CatalogueClient

    /**
     * The [CatalogueService] to test.
     */
    private lateinit var service: CatalogueService

    /**
     * Dummy book instance.
     */
    private val book = Book(
        id = "test",
        identifiers = mapOf(Identifier.INTERNAL to "test"),
        title = "The ontdekking van de hemel",
        authors = listOf("Harry Mulisch")
    )

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        repository = mock()
        client = mock()
        service = CatalogueService(repository, client)
    }

    @Test
    fun `findById returns null on unknown identifier`() {
        runBlocking {
            assertNull(service.findById("test"))
        }
    }

    @Test
    fun `findById queries database for book`() {
        repository.stub {
            on { findByIndustryId(eq("test")) } doReturn book
        }

        runBlocking {
            assertEquals(book, service.findById("test"))
        }
    }

    @Test
    fun `findById queries external catalogue as alternative and saves`() {
        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { find(eq("test")) } doReturn book
        }

        runBlocking {
            assertEquals(book, service.findById("test"))
            verify(repository, times(1)).save(book)
        }
    }

    @Test
    fun `findById queries external catalogue for book with isbn-10`() {
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.ISBN_10 to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )

        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { find(eq("test")) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(book, service.findById("test"))
        }
    }

    @Test
    fun `findById queries external catalogue for book with isbn-13`() {
        val book = Book(
            id = "test",
            identifiers = mapOf(Identifier.ISBN_13 to "test"),
            title = "The ontdekking van de hemel",
            authors = listOf("Harry Mulisch")
        )

        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { find(eq("test")) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(book, service.findById("test"))
        }
    }

    @Test
    fun `findById returns null on failure to save`() {
        repository.stub {
            on { save(any<Book>()) } doThrow ConstraintViolationException(null)
        }

        client.stub {
            onBlocking { find(eq("test")) } doReturn book
        }

        runBlocking {
            assertNull(service.findById("test"))
        }
    }

    @Test
    fun `findById returns null on failure to save due to no identifiers`() {
        client.stub {
            onBlocking { find(eq("test")) } doReturn Book("test", emptyMap(), "", "", emptyList())
        }

        runBlocking {
            assertNull(service.findById("test"))
        }
    }

    @Test
    fun `query with keywords queries external catalogue and saves`() {
        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { query(any(), any()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(listOf(book), service.query("test"))
            verify(repository, times(1)).save(book)
        }
    }

    @Test
    fun `query with keywords with different limit`() {
        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { query(any(), any()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(listOf(book), service.query("test", max = 10))
            verify(repository, times(1)).save(book)
            verify(client, times(1)).query(any(), eq(10))
        }
    }

    @Test
    fun `query with keywords queries external catalogue and filters books that cannot be saved`() {
        repository.stub {
            on { save(any<Book>()) } doThrow ConstraintViolationException(null)
        }

        client.stub {
            onBlocking { query(any(), any()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(emptyList<Book>(), service.query("test"))
        }
    }

    @Test
    fun `query with title and author queries external catalogue and saves`() {
        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        client.stub {
            onBlocking { query(any(), any(), any()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(listOf(book), service.query("test", "test"))
            verify(repository, times(1)).save(book)
        }
    }

    @Test
    fun `query with title and author queries external catalogue and filters books that cannot be saved`() {
        repository.stub {
            on { save(any<Book>()) } doThrow ConstraintViolationException(null)
        }

        client.stub {
            onBlocking { query(any(), any(), any()) } doReturn listOf(book)
        }

        runBlocking {
            assertEquals(emptyList<Book>(), service.query("test", "test"))
        }
    }

    @Test
    fun `save returns book on success`() {
        repository.stub {
            on { save(any<Book>()) } doAnswer { it.getArgument(0) }
        }

        runBlocking {
            assertEquals(book, service.save(book))
        }
    }

    @Test
    fun `save throws catalogue service exception on failure`() {
        val exception = mock<ConstraintViolationException> {
            on { message } doReturn "Hello World"
        }
        repository.stub {
            on { save(any<Book>()) } doThrow exception
        }

        runBlocking {
            assertThrows<CatalogueServiceException.InvalidInformationException> {
                service.save(book)
            }
        }
    }
}
