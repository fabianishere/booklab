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

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.post
import nl.tudelft.booklab.backend.services.vision.VisionService
import nl.tudelft.booklab.backend.spring.inject
import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.vision.toMat

/**
 * Define vision endpoints at the current route for the REST api.
 */
fun Route.detection() {
    val vision: VisionService = application.inject()
    authenticate("rest:detection") { detect(vision) }
}

/**
 * Define the endpoint for detecting a books based on an image.
 */
internal fun Route.detect(vision: VisionService) {
    post {
        val size = call.request.header(HttpHeaders.ContentLength)?.toIntOrNull() ?: DEFAULT_BUFFER_SIZE
        val response = try {
            val image = call.receiveStream().use { it.toMat(size) }
            vision.detect(image)
        } catch (e: Throwable) {
            application.log.warn("An error occurred while processing an image", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                DetectionFailure("server_error", "An internal server error occurred.")
            )
            return@post
        }

        call.respond(DetectionResult(response.size, response))
    }

    handle {
        call.respond(
            HttpStatusCode.MethodNotAllowed,
            DetectionFailure("invalid_method", "The requested method is not allowed.")
        )
    }
}

/**
 * This class defines the shape of the detection results returned by the Detection API.
 */
data class DetectionResult(val size: Int, val results: List<Book>)

/**
 * This class defines the shape of an error that occurred during the detection of books.
 */
data class DetectionFailure(
    @JsonProperty("error")
    val type: String,
    @JsonProperty("error_description")
    val description: String
)
