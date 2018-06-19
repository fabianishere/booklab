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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.util.DataConversionException
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Test suite for the [CatalogueConversionService] class.
 */
internal class CatalogueConversionServiceTest {
    /**
     * The [CatalogueService] to use.
     */
    private lateinit var service: CatalogueService

    /**
     * The [CatalogueConversionService] to test.
     */
    private lateinit var conversionService: CatalogueConversionService

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        service = mock()
        conversionService = CatalogueConversionService(service)
    }

    @Test
    fun `convert single property as catalogue id`() {
        val book = Book("test", mapOf(Identifier.INTERNAL to "test"), "Test", null, emptyList())
        service.stub {
            onBlocking { findById(eq("test")) } doReturn book
        }

        assertEquals(book, conversionService.fromValues(listOf("test"), mock()))
    }

    @Test
    fun `convert single to book but not found`() {
        assertNull(conversionService.fromValues(listOf("test"), mock()))
    }

    @Test
    fun `fail to convert multiple values`() {
        assertNull(conversionService.fromValues(listOf("test-1", "test-2"), mock()))
    }

    @Test
    fun `fail to convert empty values`() {
        assertNull(conversionService.fromValues(emptyList(), mock()))
    }

    @Test
    fun `convert null value to empty list of strings`() {
        assertTrue(conversionService.toValues(null).isEmpty())
    }

    @Test
    fun `convert non-null value to book string`() {
        val book = Book("test", mapOf(Identifier.INTERNAL to "test"), "Test", null, emptyList())
        assertEquals("test", conversionService.toValues(book).first())
    }

    @Test
    fun `fail to convert non-Book value`() {
        assertThrows<DataConversionException> {
            conversionService.toValues("test")
        }
    }
}
