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
import nl.tudelft.booklab.catalogue.sru.Book
import nl.tudelft.booklab.catalogue.sru.Title
import nl.tudelft.booklab.catalogue.sru.TitleType

/**
 * Define vision endpoints at the current route for the REST api.
 */
fun Route.detection() {
    put {
        // We only provide a mocked interface for now.
        // As soon as the book detection algorithm is implemented,
        // we will actually return interesting results.
        call.respond(DetectionResult(listOf(
            Book(listOf(Title("De Valse Dageraad", TitleType.MAIN), Title("het leven van Hroswithus Wikalensis, wereldreiziger en geleerde.", TitleType.SUB)),
                listOf("Jan van Aken"),
                listOf("1234567890")),
            Book(listOf(Title("Kaas", TitleType.MAIN)),
                listOf("Willem Elsschot"),
                listOf("1235567890")),
            Book(listOf(Title("Een Schitterend Gebrek", TitleType.MAIN)),
                listOf("Arthur Japin"),
                listOf("1234567899")),
            Book(listOf(Title("Het Diner", TitleType.MAIN)),
                listOf("Herman Koch"),
                listOf("1134567899")))))
    }
}

data class DetectionResult (
    val results: List<Book>
)
