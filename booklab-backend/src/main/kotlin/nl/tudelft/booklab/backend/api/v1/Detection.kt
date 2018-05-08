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
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.put

/**
 * Define detection endpoints at the current route for the REST api.
 */
fun Route.detection() {
    put {
        // We only provide a mocked interface for now.
        // As soon as the book detection algorithm is implemented,
        // we will actually return interesting results.
        call.respond(DetectionResult(listOf(
            Book("9789026339592", "De dag van de doden"),
            Book("9789026336904", "Achter gesloten deuren"),
            Book("9789402752663", "Goede dochter")
        )))
    }
}

/**
 * The shape of the result given by the detection endpoint.
 */
data class DetectionResult(val results: List<Book>)

data class Book(val isbn: String, val title: String)
