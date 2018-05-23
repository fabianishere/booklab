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

import io.ktor.auth.authenticate
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.route
import nl.tudelft.booklab.backend.VisionConfiguration
import nl.tudelft.booklab.catalogue.sru.SruClient

/**
 * Describe the routes for the REST API of the BookLab backend.
 */
fun Route.api() {
    authenticate("rest:detection") {
        route("detection") {
            detection(application.attributes[VisionConfiguration.KEY])
        }
    }
    route("search") {
        search(SruClient())
    }
    meta()
}
