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
import io.ktor.routing.get
import nl.tudelft.booklab.catalogue.sru.SruClient

fun Route.search() {
    get {
        val client = SruClient()
        val title = call.parameters["title"]
        val author = call.parameters["author"]

        if (title != null && author != null) {
            call.respond(client.query(client.createQuery(title, author), 5))
        } else {
            call.respondText("Failed to process query", status = HttpStatusCode.BadRequest)
        }
    }
}
