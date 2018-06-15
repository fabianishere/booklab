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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
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
 * Test suite for the [UserService] class.
 */
internal class UserServiceTest {
    /**
     * The [UserRepository] to use.
     */
    private lateinit var repository: UserRepository

    /**
     * The [UserService] to test.
     */
    private lateinit var service: UserService

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        repository = mock()
        service = UserService(repository)
    }

    @Test
    fun `findById returns user`() {
        val user = User(1, "test@example.com", "")
        repository.stub {
            on { findById(eq(1)) } doReturn Optional.of(user)
        }

        assertEquals(user, service.findById(1))
        verify(repository, times(1)).findById(eq(1))
    }

    @Test
    fun `findById returns null on unknown user`() {
        repository.stub {
            on { findById(eq(1)) } doReturn Optional.empty<User>()
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
    fun `findByEmail returns user`() {
        val user = User(1, "test@example.com", "")
        repository.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(user, service.findByEmail("test@example.com"))
        verify(repository, times(1)).findByEmail(eq("test@example.com"))
    }

    @Test
    fun `findByEmail returns null on unknown user`() {
        repository.stub {
            on { findByEmail(eq("test@example.com")) }.doReturn<User?>(null)
        }

        assertNull(service.findByEmail("test@example.com"))
        verify(repository, times(1)).findByEmail(eq("test@example.com"))
    }

    @Test
    fun `save returns saved user`() {
        val user = User(1, "test@example.com", "")

        repository.stub {
            on { save(any<User>()) } doAnswer { it.getArgument(0) }
        }

        assertEquals(user, service.save(user))
    }

    @Test
    fun `save fails on duplicate user`() {
        val user = User(1, "test@example.com", "")

        repository.stub {
            on { existsByEmail(eq("test@example.com")) } doReturn true
        }

        assertThrows<UserServiceException.UserAlreadyExistsException> {
            service.save(user)
        }
    }

    @Test
    fun `save fails on constraint error`() {
        val user = User(1, "test", "")

        repository.stub {
            on { save(any<User>()) } doThrow ConstraintViolationException(null)
        }

        assertThrows<UserServiceException.InvalidInformationException> {
            service.save(user)
        }
    }

    @Test
    fun `save fails on constraint error 2`() {
        val user = User(1, "test", "")

        val exception = mock<ConstraintViolationException> {
            on { message } doReturn "test"
        }

        repository.stub {
            on { save(any<User>()) } doThrow exception
        }

        assertThrows<UserServiceException.InvalidInformationException> {
            service.save(user)
        }
    }
}
