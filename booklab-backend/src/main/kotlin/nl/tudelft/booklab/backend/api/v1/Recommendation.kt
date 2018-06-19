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

import io.ktor.application.call
import io.ktor.auth.oauth2.scoped
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.post
import nl.tudelft.booklab.backend.services.catalogue.CatalogueService
import nl.tudelft.booklab.backend.services.collection.BookCollectionService
import nl.tudelft.booklab.backend.spring.inject
import nl.tudelft.booklab.recommender.Recommender

/**
 * Define the endpoints for the recommendation system.
 */
fun Route.recommendation() {
    val recommender: Recommender = application.inject()
    val collections: BookCollectionService = application.inject()
    val catalogue: CatalogueService = application.inject()

    scoped("recommendation") {
        post {
            val input = try { call.receive<RecommendationRequest>() } catch (e: Exception) { null }
            val candidates = if (input?.candidates != null) {
                val res = parseBooks(catalogue, input.candidates)
                val invalid = res.entries.find { it.value == null }?.key

                if (invalid != null) {
                    call.respond(HttpStatusCode.BadRequest, InvalidRequest("The book '$invalid' could not be found in the catalogue."))
                    return@post
                }

                res.values.filterNotNull().toSet()
            } else {
                null
            }

            // Get input collection either based on input books or on a user collection
            val collection = if (input?.collection != null) {
                val res = parseBooks(catalogue, input.collection)
                val invalid = res.entries.find { it.value == null }?.key

                if (invalid != null) {
                    call.respond(HttpStatusCode.BadRequest, InvalidRequest("The book '$invalid' could not be found in the catalogue."))
                    return@post
                }

                res.values.filterNotNull().toSet()
            } else {
                val id = call.parameters["collection"]?.toIntOrNull()
                id?.let { collections.findById(it)?.books?.toSet() }
            }

            if (candidates == null || collection == null) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest("The request was of invalid format."))
                return@post
            }

            val recommendations = recommender.recommend(collection, candidates.toSet())
            call.respond(Success(recommendations, meta = mapOf("count" to recommendations.size)))
        }

        handle { call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed()) }
    }
}

/**
 * A request for generating recommendations for a book collection.
 */
data class RecommendationRequest(val collection: List<String>?, val candidates: List<String>)
