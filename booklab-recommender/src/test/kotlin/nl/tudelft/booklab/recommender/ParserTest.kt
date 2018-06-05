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

package nl.tudelft.booklab.recommender

import nl.tudelft.booklab.recommender.rating.goodreads.GoodreadsParser
import nl.tudelft.booklab.recommender.rating.goodreads.Rating
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParserTest {
    @Test
    fun `single result`() {
        val results = GoodreadsParser().parse(ParserTest::class.java.getResourceAsStream("/goodreads-single.json"))

        assertEquals(results.ratings.size, 1)
        assertEquals(results.ratings[0], Rating("8700631620", "9788700631625", "4.45"))
    }

    @Test
    fun `multiple results`() {
        val results = GoodreadsParser().parse(
            ParserTest::class.java.getResourceAsStream("/goodreads-multiple.json"))

        assertEquals(results.ratings.size, 2)
        assertEquals(results.ratings[0], Rating("8700631620", "9788700631625", "4.45"))
        assertEquals(results.ratings[1], Rating("2070733955", "9782070733958", "4.17"))
    }
}
