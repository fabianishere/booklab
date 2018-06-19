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
import io.ktor.auth.oauth2.scoped
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.route
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.spring.inject
import io.ktor.locations.get as getLocation

/**
 * Define catalogue endpoints at the current route for the REST api.
 */
fun Route.catalogue() {
    scoped("catalogue") {
        catalogueCollection()
        catalogueResource()
    }
}

/**
 * Define an endpoint for querying the catalogue as collection.
 */
internal fun Route.catalogueCollection() {
    val catalogue: CatalogueService = application.inject()

    get {
        val query = call.parameters["query"]
        val title = call.parameters["title"]
        val author = call.parameters["author"]
        val max = call.parameters["max"]?.toIntOrNull() ?: 5

        if (query != null) {
            val results = catalogue.query(query, max)
            call.respond(Success(results, meta = mapOf("count" to results.size)))
        } else if (title != null && author != null) {
            val results = catalogue.query(title, author, max)
            call.respond(Success(results, meta = mapOf("count" to results.size)))
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                InvalidRequest("No valid query given.")
            )
        }
    }

    handle {
        call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed())
    }
}

/**
 * Define an endpoint for accessing a single resource in the catalogue.
 */
internal fun Route.catalogueResource() {
    getLocation<CatalogueBookRoute> { (book) ->
        val id = call.parameters["collection"]
        if (book == null) {
            call.respond(HttpStatusCode.NotFound, NotFound("The collection '$id' was not found on the server."))
            return@getLocation
        }

        call.respond(Success(book))
    }

    route("/{collection}") {
        handle {
            call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed())
        }
    }
}

/**
 * The route to a collection in the catalogue.
 */
@Location("/{book}")
data class CatalogueBookRoute(val book: Book?)
