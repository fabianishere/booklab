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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeParseException

/**
 * Test suite for the [JacksonPublishDateDeserializer] class.
 */
internal class JacksonPublishDateDeserializerTest {
    /**
     * The [JsonParser] instance to use.
     */
    private lateinit var parser: JsonParser

    /**
     * The [ObjectCodec] to give to the deserializer.
     */
    private lateinit var codec: ObjectCodec

    /**
     * The [JsonNode] to represent the value to parse.
     */
    private lateinit var node: JsonNode

    /**
     * The dummy [DeserializationContext] to pass in.
     */
    private lateinit var context: DeserializationContext

    /**
     * The [JpaPublishDateConverter] to test.
     */
    private lateinit var converter: JacksonPublishDateDeserializer

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        converter = JacksonPublishDateDeserializer()
        node = mock()
        codec = mock {
            on { readTree<JsonNode>(any()) } doReturn node
        }
        parser = mock {
            on { codec } doReturn codec
        }
        context = mock()
    }

    @Test
    fun `converter should parse year as Year`() {
        node.stub {
            on { asText() } doReturn "2017"
        }
        assertTrue(converter.deserialize(parser, context) is Year)
    }

    @Test
    fun `converter should parse year-month as YearMonth`() {
        node.stub {
            on { asText() } doReturn "2017-10"
        }
        assertTrue(converter.deserialize(parser, context) is YearMonth)
    }

    @Test
    fun `converter should parse iso date as LocalDate`() {
        node.stub {
            on { asText() } doReturn "2017-10-12"
        }
        assertTrue(converter.deserialize(parser, context) is LocalDate)
    }

    @Test
    fun `converter crash on invalid input`() {
        node.stub {
            on { asText() } doReturn "20"
        }
        assertThrows<DateTimeParseException> {
            converter.deserialize(parser, context)
        }
    }
}
