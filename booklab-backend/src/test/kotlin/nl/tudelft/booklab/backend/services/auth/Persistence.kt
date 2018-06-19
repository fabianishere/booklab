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

package nl.tudelft.booklab.backend.services.auth

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import io.ktor.auth.Principal
import io.ktor.auth.UserPasswordCredential
import io.ktor.auth.oauth2.repository.UserRepository
import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.backend.services.password.PasswordService
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test suite for the [PersistentUserRepository] class.
 */
internal class PersistentUserRepositoryTest {
    /**
     * The [UserService] that is used by the repository.
     */
    private lateinit var userService: UserService

    /**
     * The [PasswordService] that is used by the repository.
     */
    private lateinit var passwordService: PasswordService

    /**
     * The [PersistentUserRepository] to test.
     */
    private lateinit var repository: PersistentUserRepository

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        userService = mock()
        passwordService = mock()
        repository = PersistentUserRepository(userService, passwordService)
    }

    @Test
    fun `repository should return email as identifier`() {
        val user = User(0, "test@example.com", "")
        @Suppress("UNCHECKED_CAST")
        assertEquals(user.email, (repository as UserRepository<Principal>).run { (user as Principal).id })
    }

    @Test
    fun `repository should lookup user with user service`() {
        val user = User(0, "test@example.com", "")

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(user, runBlocking { repository.lookup(user.email) })
    }

    @Test
    fun `repository should return null for invalid user`() {
        val user = User(0, "test@example.com", "")

        assertEquals(null, runBlocking { repository.lookup(user.email) })
    }

    @Test
    fun `repository should lookup user with user service during authorization validation`() {
        val user = User(0, "test@example.com", "")

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(user, runBlocking { repository.validate(UserPasswordCredential(user.email, ""), authorize = true) })
    }

    @Test
    fun `repository should lookup user with user service during validation with invalid password`() {
        val user = User(0, "test@example.com", "")

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(null, runBlocking { repository.validate(UserPasswordCredential(user.email, "")) })
    }

    @Test
    fun `repository should lookup user with user service during validation`() {
        val user = User(0, "test@example.com", "")

        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        passwordService.stub {
            on { verify(any(), any()) } doReturn true
        }

        assertEquals(user, runBlocking { repository.validate(UserPasswordCredential(user.email, "")) })
    }
}
