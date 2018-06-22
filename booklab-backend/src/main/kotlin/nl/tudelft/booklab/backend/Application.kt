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

package nl.tudelft.booklab.backend

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth2.oauth
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.DataConversion
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.routing.routing
import nl.tudelft.booklab.backend.api.v1.api
import nl.tudelft.booklab.backend.ktor.Routes
import nl.tudelft.booklab.backend.ktor.TypedConversionService
import nl.tudelft.booklab.backend.services.auth.BooklabOAuthServer
import nl.tudelft.booklab.backend.spring.inject
import nl.tudelft.booklab.backend.spring.injectAll

/**
 * Configure the given Ktor [Application] as Booklab backend application.
 */
fun Application.booklab() {
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(SimpleModule().apply {
                val deserializers: List<StdDeserializer<*>> = injectAll()
                for (deserializer in deserializers) {
                    // We can suppress the cast as we know it must be correct for StdDeserializer instances since they
                    // specify their types during construction.
                    @Suppress("UNCHECKED_CAST")
                    addDeserializer(deserializer.handledType() as Class<Any>, deserializer)
                }
            })
        }
    }
    install(Authentication) {
        oauth(inject<BooklabOAuthServer>())
    }
    install(Locations)
    install(DataConversion) {
        val services: List<TypedConversionService> = injectAll()
        for (service in services) {
            for (type in service.types) {
                convert(type, service)
            }
        }
    }

    // Allow the different hosts to connect to the REST API
    install(CORS) {
        anyHost()

        // Allow the Authorization header to be sent to REST endpoints
        header(HttpHeaders.Authorization)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
    }

    inject<Routes>("routes").run {
        routing { configure() }
    }
}

/**
 * Retrieve the base url of the server.
 */
val Application.baseUrl: String get() = environment.config.propertyOrNull("ktor.deployment.base-url")?.getString() ?: ""

/**
 * The routes of the application.
 */
internal fun Routing.routes() {
    route("/api") { api() }
}
