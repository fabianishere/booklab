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

package nl.tudelft.booklab.catalogue.sru

import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SruParserTest {

    private lateinit var books: List<Book>
    private lateinit var realBooks: List<Book>

    @BeforeEach
    fun setUp() {
        books = SruParser.parse(SruParserTest::class.java.getResource("/synthetic-test.xml").openStream())
        realBooks = SruParser.parse(SruParserTest::class.java.getResource("/realistic-test.xml").openStream())
    }

    @Test
    fun `smoke test`() {
        val result = books[0]

        assertEquals("author name", result.authors[0])
        assertEquals("0123456789", result.identifiers[Identifier.ISBN_10])
        assertEquals("main title", result.title)
        assertEquals("sub title", result.subtitle)
    }

    @Test
    fun `no author`() {
        val result = books[1]

        assertEquals(0, result.authors.size)
    }

    @Test
    fun `multiple authors`() {
        val result = books[2]

        assertEquals(2, result.authors.size)
        assertEquals("author 1", result.authors[0])
        assertEquals("author 2", result.authors[1])
    }

    @Test
    fun `no isbn`() {
        val result = books[3]

        assertEquals(0, result.identifiers.size)
    }

    @Test
    fun `multiple isbn`() {
        val result = books[4]

        assertEquals(1, result.identifiers.size)
    }

    @Test
    fun `no titles`() {
        val result = books[5]

        assertEquals("<NO TITLE>", result.title)
    }

    @Test
    fun `realistic test (de ontdekking van de hemel)`() {
        assertEquals(20, realBooks.size)

        assertEquals("De ontdekking van de hemel", realBooks[0].title)
        assertEquals("Cornelissen, Ignace", realBooks[0].authors[0])
        assertEquals("9789491396311", realBooks[0].identifiers[Identifier.ISBN_13])
    }
}
