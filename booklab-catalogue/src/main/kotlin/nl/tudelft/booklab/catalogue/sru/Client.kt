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

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get

/**
 * A SRU client that is used to query books from a SRU database
 *
 * @param client the client used to send a HTTP request to the database
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class SruClient(private val client: HttpClient = HttpClient(Apache)) {

    /**
     * Queries the a SRU database using the given query
     *
     * @param query the actual query string that would normally be used in browser
     *
     * @return the list of books returned from the query
     */
    suspend fun query(query: String): List<Book> {
        return SruParser.parse(client.get<String>(query))
    }
}
