/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.booklab.catalogue.sru

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

internal class ParserTest {

    private val books = SruParser.parse(ParserTest::class.java.getResource("/synthetic-test.xml").openStream())
    private val realBooks = SruParser.parse(ParserTest::class.java.getResource("/realistic-test.xml").openStream())

    @Test
    fun `smoke test`() {
        val result = books[0]

        assertThat(result.authors[0], equalTo("author name"))
        assertThat(result.ids[0], equalTo("0123456789"))
        assertThat(result.titles[0].type, equalTo(TitleType.MAIN))
        assertThat(result.titles[0].value, equalTo("main title"))
        assertThat(result.titles[1].type, equalTo(TitleType.SUB))
        assertThat(result.titles[1].value, equalTo("sub title"))
    }

    @Test
    fun `no author`() {
        val result = books[1]

        assertThat(result.authors.size, equalTo(0))
    }

    @Test
    fun `multiple authors`() {
        val result = books[2]

        assertThat(result.authors.size, equalTo(2))
        assertThat(result.authors[0], equalTo("author 1"))
        assertThat(result.authors[1], equalTo("author 2"))
    }

    @Test
    fun `no isbn`() {
        val result = books[3]

        assertThat(result.ids.size, equalTo(0))
    }

    @Test
    fun `multiple isbn`() {
        val result = books[4]

        assertThat(result.ids.size, equalTo(2))
        assertThat(result.ids[0], equalTo("1234"))
        assertThat(result.ids[1], equalTo("5678"))
    }

    @Test
    fun `no titles`() {
        val result = books[5]

        assertThat(result.titles.size, equalTo(0))
    }

    @Test
    fun `multiple titles`() {
        val result = books[6]

        assertThat(result.titles.size, equalTo(2))
    }

    @Test
    fun `multiple elements but no in a row`() {
        val result = books[7]

        assertThat(result.authors.size, equalTo(3))
        assertThat(result.titles.size, equalTo(3))
        assertThat(result.ids.size, equalTo(4))
    }

    @Test
    fun `realistic test (de ontdekking van de hemel)`() {
        assertThat(realBooks.size, equalTo(20))

        assertThat(realBooks[0].titles[0].value, equalTo("De ontdekking van de hemel"))
        assertThat(realBooks[0].titles[0].type, equalTo(TitleType.MAIN))
        assertThat(realBooks[0].authors[0], equalTo("Cornelissen, Ignace"))
        assertThat(realBooks[0].ids[0], equalTo("9789491396311"))
    }
}
