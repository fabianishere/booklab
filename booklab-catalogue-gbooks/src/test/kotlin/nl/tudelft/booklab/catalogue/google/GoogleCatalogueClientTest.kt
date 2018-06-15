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

package nl.tudelft.booklab.catalogue.google

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.books.Books
import com.google.api.services.books.BooksRequestInitializer
import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.catalogue.CatalogueClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GoogleCatalogueClientTest {
    lateinit var client: CatalogueClient

    @BeforeEach
    fun setUp() {
        val key = System.getenv()["GOOGLE_BOOKS_API_KEY"]
        assumeTrue(key != null, "No Google Books API key given for running the Google Books tests (key GOOGLE_BOOKS_API_KEY)")
        val books = Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
            .setApplicationName("booklab")
            .setGoogleClientRequestInitializer(BooksRequestInitializer(key))
            .build()
        client = GoogleCatalogueClient(books)
    }

    @Test
    fun `default query test`() {
        runBlocking {
            val results = client.query("harry potter steen der wijzen", 5)

            assertTrue(results.size > 2)
        }
    }

    @Test
    fun `specific book search`() {
        runBlocking {
            val results = client.query("de ontdekking van de hemel", "harry mullish", 5)

            assertEquals(2, results.size)
        }
    }
}
