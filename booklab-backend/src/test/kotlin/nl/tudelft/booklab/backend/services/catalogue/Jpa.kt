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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeParseException

/**
 * Test suite for the [JpaPublishDateConverter] class.
 */
internal class JpaPublishDateConverterTest {
    /**
     * The [JpaPublishDateConverter] to test.
     */
    private lateinit var converter: JpaPublishDateConverter

    /**
     * Setup the test environment.
     */
    @BeforeEach
    fun setUp() {
        converter = JpaPublishDateConverter()
    }

    @Test
    fun `converter should parse year as Year`() {
        assertTrue(converter.convertToEntityAttribute("2017") is Year)
    }

    @Test
    fun `converter should parse year-month as YearMonth`() {
        assertTrue(converter.convertToEntityAttribute("2017-12") is YearMonth)
    }

    @Test
    fun `converter should parse iso date as LocalDate`() {
        assertTrue(converter.convertToEntityAttribute("2017-12-10") is LocalDate)
    }

    @Test
    fun `converter should not parse null`() {
        assertNull(converter.convertToEntityAttribute(null))
    }

    @Test
    fun `converter crash on invalid input`() {
        assertThrows<DateTimeParseException> {
            converter.convertToEntityAttribute("2")
        }
    }

    @Test
    fun `converter should convert Year to year`() {
        assertEquals("2017", converter.convertToDatabaseColumn(Year.of(2017)))
    }

    @Test
    fun `converter should convert YearMonth to year-month`() {
        assertEquals("2017-10", converter.convertToDatabaseColumn(YearMonth.of(2017, 10)))
    }

    @Test
    fun `converter should convert LocalDate to iso format`() {
        assertEquals("2017-10-12", converter.convertToDatabaseColumn(LocalDate.of(2017, 10, 12)))
    }

    @Test
    fun `converter should not convert null`() {
        assertNull(converter.convertToDatabaseColumn(null))
    }
}
