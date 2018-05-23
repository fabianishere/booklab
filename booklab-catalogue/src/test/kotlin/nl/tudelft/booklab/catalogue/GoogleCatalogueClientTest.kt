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

package nl.tudelft.booklab.catalogue

import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.catalogue.google.GoogleCatalogueClient
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class GoogleCatalogueClientTest {
    private val googleClient = GoogleCatalogueClient()

    @Test
    fun `default query test`() {
        runBlocking {
            val results = googleClient.query("harry potter steen der wijzen", 5)

            assertThat(results.size, equalTo(4))
        }
    }

    @Test
    fun `specific book search`() {
        runBlocking {
            val results = googleClient.query("de ontdekking van de hemel", "harry mullish", 5)

            assertThat(results.size, equalTo(5))
        }
    }
}
