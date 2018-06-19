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
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.util.DataConversionException
import nl.tudelft.booklab.backend.services.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Test suite for the [BookCollectionConversionService] class.
 */
internal class BookCollectionConversionServiceTest {
    /**
     * The [BookCollectionService] to use.
     */
    private lateinit var service: BookCollectionService

    /**
     * The [BookCollectionConversionService] to test.
     */
    private lateinit var conversionService: BookCollectionConversionService

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        service = mock()
        conversionService = BookCollectionConversionService(service)
    }

    @Test
    fun `convert single property as catalogue id`() {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(1, user, "test", emptySet())
        service.stub {
            onBlocking { findById(eq(1)) } doReturn collection
        }

        assertEquals(collection, conversionService.fromValues(listOf("1"), mock()))
    }

    @Test
    fun `convert single to book but not found`() {
        assertNull(conversionService.fromValues(listOf("test"), mock()))
    }

    @Test
    fun `fail to convert multiple values`() {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(1, user, "test", emptySet())
        service.stub {
            onBlocking { findById(eq(1)) } doReturn collection
        }
        assertNull(conversionService.fromValues(listOf("1", "test-2"), mock()))
    }

    @Test
    fun `fail to convert empty values`() {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(0, user, "test", emptySet())
        service.stub {
            onBlocking { findById(any()) } doReturn collection
        }
        assertNull(conversionService.fromValues(emptyList(), mock()))
    }

    @Test
    fun `convert null value to empty list of strings`() {
        assertTrue(conversionService.toValues(null).isEmpty())
    }

    @Test
    fun `convert non-null value to book string`() {
        val user = User(1, "test@example.com", "")
        val collection = BookCollection(1, user, "test", emptySet())
        assertEquals("1", conversionService.toValues(collection).first())
    }

    @Test
    fun `fail to convert non-Book value`() {
        assertThrows<DataConversionException> {
            conversionService.toValues("test")
        }
    }
}
