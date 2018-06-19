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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test suite for the [JacksonUserDeserializer] class.
 */
internal class JacksonUserDeserializerTest {
    /**
     * The [ObjectMapper] instance to use.
     */
    private lateinit var mapper: ObjectMapper

    /**
     * The [UserService] used to lookup the user.
     */
    private lateinit var userService: UserService

    /**
     * The [JacksonUserDeserializer] to test.
     */
    private lateinit var deserializer: JacksonUserDeserializer

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        userService = mock()
        deserializer = JacksonUserDeserializer(userService)
        mapper = jacksonObjectMapper()
        mapper.registerModule(SimpleModule().apply {
            addDeserializer(User::class.java, deserializer)
        })
    }

    @Test
    fun `valid id converts to user`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findById(eq(1)) } doReturn user
        }

        assertEquals(user, mapper.readValue("""{ "id" : 1 }"""))
    }

    @Test
    fun `coerce id from string to integer and return user`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findById(eq(1)) } doReturn user
        }

        assertEquals(user, mapper.readValue("""{ "id" : "1" }"""))
    }

    @Test
    fun `failed to coerce invalid id to integer and return null`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findById(eq(1)) } doReturn user
        }

        assertNull(mapper.readValue("""{ "id" : "test" }""", User::class.java))
    }

    @Test
    fun `failed to coerce invalid id to integer and try email`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findById(eq(1)) } doReturn user
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(user, mapper.readValue("""{ "id" : "test", "email" : "test@example.com" }"""))
    }

    @Test
    fun `failed to coerce invalid id to integer and fail to parse email returns null`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findById(eq(1)) } doReturn user
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertNull(mapper.readValue("""{ "id" : "test", "email" : 1 }""", User::class.java))
    }

    @Test
    fun `convert email and return user`() {
        val user = User(1, "test", "")
        userService.stub {
            on { findByEmail(eq("test@example.com")) } doReturn user
        }

        assertEquals(user, mapper.readValue("""{ "email" : "test@example.com" }"""))
    }
}
