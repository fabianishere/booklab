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

package nl.tudelft.booklab.backend.services.user

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.util.DataConversionException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Test suite for the [UserConversionService] class.
 */
internal class UserConversionServiceTest {
    /**
     * The [UserService] to use.
     */
    private lateinit var service: UserService

    /**
     * The [UserConversionService] to test.
     */
    private lateinit var conversionService: UserConversionService

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        service = mock()
        conversionService = UserConversionService(service)
    }

    @Test
    fun `convert single property as user id`() {
        val user = User(1, "test", "")
        service.stub {
            on { findById(eq(1)) } doReturn user
        }

        assertEquals(user, conversionService.fromValues(listOf("1"), mock()))
    }

    @Test
    fun `convert single to user but not found`() {
        assertNull(conversionService.fromValues(listOf("1"), mock()))
    }

    @Test
    fun `fail to convert non-integer value`() {
        assertNull(conversionService.fromValues(listOf("test"), mock()))
    }

    @Test
    fun `fail to convert multiple values`() {
        assertNull(conversionService.fromValues(listOf("1", "2"), mock()))
    }

    @Test
    fun `fail to convert empty values`() {
        assertNull(conversionService.fromValues(emptyList(), mock()))
    }

    @Test
    fun `convert null value to empty list of strings`() {
        Assertions.assertTrue(conversionService.toValues(null).isEmpty())
    }

    @Test
    fun `convert non-null value to user string`() {
        val user = User(1, "test", "")
        assertEquals("1", conversionService.toValues(user).first())
    }

    @Test
    fun `fail to convert non-user value`() {
        assertThrows<DataConversionException> {
            conversionService.toValues("1")
        }
    }
}
