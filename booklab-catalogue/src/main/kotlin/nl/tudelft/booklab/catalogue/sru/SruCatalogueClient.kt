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
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.config
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.experimental.io.jvm.javaio.toInputStream
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient

/**
 * A SRU client that is used to query lists of [Book]s from a SRU catalogue.
 * it implements the [CatalogueClient] interface
 *
 * @property client the HTTP client used to send requests to the catalogue.
 * the default client uses Apache
 * @property baseUrl the base url to the sru catalogue. the value defaults
 * to http://jsru.kb.nl/sru the SRU catalogue of the Koninklijke Bibliotheek
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class SruCatalogueClient (
    private val client: HttpClient = HttpClient(Apache.config { socketTimeout = 100000 }),
    private val baseUrl: String = "http://jsru.kb.nl/sru"
) : CatalogueClient {

    override suspend fun query(keywords: String, max: Int): List<Book> {
        return queryHelper(createCqlQuery(keywords), max)
    }

    override suspend fun query(title: String, author: String, max: Int): List<Book> {
        return queryHelper(createCqlQuery(title, author), max)
    }

    /**
     * helper method used to avoid code duplication. this method actually queries
     * the catalogue and parses it to the list of [Book]s
     *
     * @param cqlQuery a CQL query. see the following link for the specification
     * http://www.loc.gov/standards/sru/cql/index.html
     * @param max the maximum amount of results
     * @return the list of matching [Book]s
     */
    private suspend fun queryHelper(cqlQuery: String, max: Int): List<Book> {
        val stream = client.call {
            url(createSruUrl(cqlQuery.toLowerCase(), max))
            method = HttpMethod.Get
        }.response.content.toInputStream()
        return DublinCoreParser.parse(stream)
    }

    /**
     * creates a CQL query based on the authors name and the
     * book title
     *
     * @param title keywords matching the title
     * @param author keywords matching the author name
     * @return a string represented CQL query
     */
    private fun createCqlQuery(title: String, author: String): String {
        // CQL version 1.2 is used see the link below for more info
        // http://www.loc.gov/standards/sru/cql/contextSets/cql-context-set-v1-2.html
        return """dc.title any/fuzzy/ignoreCase/ignoreAccents "$title" OR
            |dc.creator any/fuzzy/ignoreCase/ignoreAccents "$author"""".trimMargin()
    }

    /**
     * creates a CQL query based on keywords
     *
     * @param keywords some keywords matching the book
     * @return a string represented CQl query
     */
    private fun createCqlQuery(keywords: String): String {
        return createCqlQuery(keywords, keywords)
    }

    /**
     * creates a SRU URL
     *
     * @param query the CQL query
     * @param max the maximum number of records to be received
     * @return a string representation of the the url
     */
    private fun createSruUrl(query: String, max: Int): String {
        val params = listOf(
            "operation" to "searchRetrieve",
            "version" to "1.2",
            "recordSchema" to "dcx",
            "x-collection" to "GGC",
            "query" to query,
            "maximumRecords" to max.toString())
        return "$baseUrl?${params.formUrlEncode()}"
    }
}
