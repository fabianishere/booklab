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

package nl.tudelft.booklab.backend.api.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.get
import nl.tudelft.booklab.backend.spring.inject
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.CatalogueClient

/**
 * Define catalogue search endpoints at the current route for the REST api.
 */
fun Route.search() {
    val catalogue: CatalogueClient = application.inject()

    get {
        val title = call.parameters["title"]
        val author = call.parameters["author"]
        val max = call.parameters["max"]?.toIntOrNull() ?: 5

        if (title != null && author != null) {
            val results = catalogue.query(title, author, max)
            call.respond(SearchResults(results.size, results))
        } else {
            call.respondText("Failed to process query", status = HttpStatusCode.BadRequest)
        }
    }
}

/**
 * This class defines the shape of the search results returned by the Search API.
 */
data class SearchResults(val size: Int, val results: List<Book>)
