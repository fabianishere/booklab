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

package nl.tudelft.booklab.catalogue.google

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.books.Books
import com.google.api.services.books.BooksRequestInitializer
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Client
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.catalogue.TitleType

class GoogleClient : Client {
    private val key = ""
    private val database = Books.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        null)
            .setApplicationName("booklab")
            .setGoogleClientRequestInitializer(BooksRequestInitializer(key))
            .build()

    override fun query(query: String): List<Book> {
        return database.volumes().list(query).setMaxResults(40).execute().items
            .asSequence()
            .filter { it.volumeInfo.industryIdentifiers != null }
            .filter { it.volumeInfo.authors != null }
            .filter { it.volumeInfo.title != null }
            .map { Book(
                    listOfNotNull(
                        Title(it.volumeInfo.title, TitleType.MAIN),
                        it.volumeInfo.subtitle?.let { Title(it, TitleType.SUB) }
                    ),
                    it.volumeInfo.authors,
                    it.volumeInfo.industryIdentifiers
                        .filter { it.type == "ISBN_10" || it.type == "ISBN_13" }
                        .map { it.identifier })
        }.toList()
    }

    override fun query(title: String, author: String): List<Book> {
        return query("intitle:$title+inauthor:$author")
    }
}

