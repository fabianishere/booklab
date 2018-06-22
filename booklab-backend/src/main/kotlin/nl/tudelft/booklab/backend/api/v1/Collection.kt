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

package nl.tudelft.booklab.backend.api.v1

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.oauth2.AccessToken
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.scoped
import io.ktor.auth.principal
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.locations.Location
import io.ktor.locations.locations
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import nl.tudelft.booklab.backend.baseUrl
import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.services.collection.BookCollection
import nl.tudelft.booklab.backend.services.collection.BookCollectionService
import nl.tudelft.booklab.backend.services.collection.BookCollectionServiceException
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import nl.tudelft.booklab.backend.spring.inject
import io.ktor.locations.delete as deleteLocation
import io.ktor.locations.get as getLocation
import io.ktor.locations.post as postLocation
import io.ktor.locations.put as putLocation

/**
 * Define collection endpoints at the current route of the REST API.
 */
fun Route.collections() {
    scoped("collection") {
        collectionSearch()
        collectionCreate()
        collectionResource()
        handle { call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed()) }
    }
}

/**
 * Define the endpoint for searching for collections.
 */
internal fun Route.collectionSearch() {
    val userService: UserService = application.inject()

    // Search by user
    get {
        val id = call.parameters["user"]
        val user = id?.toIntOrNull()?.let { userService.findById(it) }

        if (user == null) {
            call.respond(HttpStatusCode.NotFound, NotFound("The collections where not found for user $id"))
            return@get
        }

        call.respond(Success(user.collections, meta = mapOf("count" to user.collections.size)))
    }
}

/**
 * Define the endpoint to create a collection.
 */
internal fun Route.collectionCreate() {
    val catalogueService: CatalogueService = application.inject()
    val collectionService: BookCollectionService = application.inject()

    // SECTION: Creating a collection
    val handler = {
        post {
            // Validate whether the current user can modify the collection
            val principal = call.principal<AccessToken<ClientIdPrincipal, User>>()?.user
            if (principal == null) {
                call.respond(HttpStatusCode.Forbidden, Forbidden())
                return@post
            }

            val request: BookCollectionCreateRequest? = try { call.receive() } catch (e: Exception) { null }
            if (request == null) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest("The body of the content is invalid."))
                return@post
            }

            // Parse the books in the request
            val books = let {
                val books = parseBooks(catalogueService, request.books)
                val invalid = books.entries.find { it.value == null }?.key

                if (invalid != null) {
                    call.respond(HttpStatusCode.BadRequest, InvalidRequest("The collection '$invalid' could not be found in the catalogue."))
                    return@post
                }

                books.values.filterNotNull().toSet()
            }

            try {
                val collection = collectionService.save(BookCollection(-1, principal, request.name, books))
                val location = URLBuilder().run {
                    takeFrom(application.baseUrl)
                    encodedPath = call.request.uri.removeSurrounding("/") + call.locations.href(CollectionRoute(collection))
                    buildString()
                }
                call.response.header("Location", location)
                call.respond(HttpStatusCode.Created, Success(collection))
            } catch (e: BookCollectionServiceException.InvalidInformationException) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest(e.message))
            } catch (e: BookCollectionServiceException.BookCollectionAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, ResourceAlreadyExists(e.message))
            } catch (e: Exception) {
                application.log.warn("An unexpected error occurred", e)
                call.respond(HttpStatusCode.InternalServerError, ServerError())
            }
        }
    }

    accept(ContentType.Application.Json) { handler() }
}

/**
 * Retrieve the collection of the given identifier.
 */
internal fun Route.collectionResource() {
    val catalogueService: CatalogueService = application.inject()
    val collectionService: BookCollectionService = application.inject()

    // Interceptor to validate parameter
    route("/{collection}") {
        intercept(ApplicationCallPipeline.Call) {
            val param = call.parameters["collection"]!!
            val id = param.toIntOrNull()

            if (id == null || !collectionService.existsById(id)) {
                call.respond(
                    HttpStatusCode.NotFound,
                    NotFound("The collection '$param' was not found on the server.")
                )
                finish()
            }
        }
    }

    // An endpoint for retrieving the whole collection
    getLocation<CollectionRoute> { (collection) ->
        call.respond(Success(collection))
    }

    // An endpoint for retrieving the books of a collection
    getLocation<CollectionBooksRoute> { (collection) ->
        call.respond(Success(collection.books, meta = mapOf("count" to collection.books.size)))
    }

    // A handler for modification of books in a collection
    val handler: suspend PipelineContext<Unit, ApplicationCall>.(CollectionBooksRoute) -> Unit = handler@{ (collection) ->
        // Validate whether the current user can modify the collection
        val principal = call.principal<AccessToken<ClientIdPrincipal, User>>()?.user
        if (principal == null || collection.user?.id != principal.id) {
            call.respond(HttpStatusCode.Forbidden, Forbidden())
            return@handler
        }

        val request: BookCollectionBooksModificationRequest? = try { call.receive() } catch (e: Exception) { null }
        if (request == null) {
            call.respond(HttpStatusCode.BadRequest, InvalidRequest("The body of the content is invalid."))
            return@handler
        }

        // Parse the books in the request
        val books = let {
            val books = parseBooks(catalogueService, request.books)
            val invalid = books.entries.find { it.value == null }?.key

            if (invalid != null) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest("The collection '$invalid' could not be found in the catalogue."))
                return@handler
            }

            books.values.filterNotNull().toSet()
        }

        try {
            val res = when (call.request.httpMethod) {
                HttpMethod.Post -> collectionService.addBooks(collection, books)
                HttpMethod.Put -> collectionService.setBooks(collection, books)
                HttpMethod.Delete -> collectionService.deleteBooks(collection, books)
                else -> null // UNREACHABLE
            }
            call.respond(Success(res))
        } catch (e: BookCollectionServiceException.InvalidInformationException) {
            call.respond(HttpStatusCode.BadRequest, InvalidRequest(e.message))
        } catch (e: Exception) {
            application.log.warn("An unexpected error occurred", e)
            call.respond(HttpStatusCode.InternalServerError, ServerError())
        }
    }

    // An endpoint for modifying the books of a collection
    postLocation(handler)
    putLocation(handler)
    deleteLocation(handler)

    route("/{collection}") {
        handle { call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed()) }

        route("/{collection}/books") {
            handle { call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed()) }
        }
    }
}

/**
 * Parse the books from a request.
 *
 * @param bookService The collection service to lookup the books.
 * @param books The books to parse.
 * @return The list of books that has been parsed.
 */
internal suspend fun parseBooks(bookService: CatalogueService, books: List<String>): Map<String, Book?> =
    books.map { it to bookService.findById(it) }.toMap()

/**
 * The route to a collection.
 *
 * @property collection The collection that was referenced.
 */
@Location("/{collection}")
data class CollectionRoute(val collection: BookCollection)

/**
 * The route to the books in a collection.
 */
@Location("/{collection}/books")
data class CollectionBooksRoute(val collection: BookCollection)

/**
 * A request sent by the user to create a new collection of books.
 *
 * @property name The name of the collection.
 * @property books The isbns of the books in the collection.
 */
data class BookCollectionCreateRequest(
    val name: String,
    val books: List<String>
)

/**
 * A request sent by the user to modify books in his collection.
 *
 * @property books The isbns of the books to add.
 */
data class BookCollectionBooksModificationRequest(val books: List<String>)
