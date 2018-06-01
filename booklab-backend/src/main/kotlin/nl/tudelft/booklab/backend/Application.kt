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

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.books.Books
import com.google.api.services.books.BooksRequestInitializer
import com.google.cloud.vision.v1.ImageAnnotatorClient
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.oauth2.oauth
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import nl.tudelft.booklab.backend.api.v1.api
import nl.tudelft.booklab.backend.auth.OAuthConfiguration
import nl.tudelft.booklab.backend.auth.asOAuthConfiguration
import nl.tudelft.booklab.catalogue.google.GoogleCatalogueClient
import nl.tudelft.booklab.vision.detection.tensorflow.TensorflowBookDetector
import nl.tudelft.booklab.vision.ocr.gvision.GoogleVisionTextExtractor
import org.tensorflow.Graph

/**
 * The main entry point of the BookLab web application.
 */
fun Application.booklab() {
    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        configureJackson()
    }

    install(Authentication) {
        val oauth = environment.config.config("auth").asOAuthConfiguration().also {
            attributes.put(OAuthConfiguration.KEY, it)
        }
        configureOAuth(oauth)
    }

    // Allow the different hosts to connect to the REST API
    install(CORS) {
        anyHost()

        // Allow the Authorization header to be sent to REST endpoints
        header(HttpHeaders.Authorization)
        method(HttpMethod.Post)
    }

    configureCatalogue()
    configureVision()

    routing {
        route("/api") {
            api()
        }
    }
}

/**
 * Configure the Jackson support for the [ContentNegotiation] feature.
 */
fun ContentNegotiation.Configuration.configureJackson() {
    jackson {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        registerModule(JavaTimeModule())
    }
}

/**
 * Configure the OAuth authentication providers for an application.
 *
 * @param oauth The [OAuthConfiguration] to use for the authentication provider.
 */
fun Authentication.Configuration.configureOAuth(oauth: OAuthConfiguration) {
    // Create an unnamed authentication provider for protecting resources using
    // the OAuth authorization server.
    oauth<ClientIdPrincipal, UserIdPrincipal>("rest:detection") {
        server = oauth.server
        scopes = setOf("detection")
    }
}

/**
 * Configure the [CatalogueConfiguration] of the [Application].
 */
fun Application.configureCatalogue() {
    // Define the catalogue configuration
    val catalogue = CatalogueConfiguration(
        client = GoogleCatalogueClient(
            Books.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null
            )
                .setApplicationName("booklab")
                .setGoogleClientRequestInitializer(BooksRequestInitializer(environment.config.property("catalogue.key").getString()))
                .build()
        )
    ).also {
        attributes.put(CatalogueConfiguration.KEY, it)
    }
}

/**
 * Configure the [VisionConfiguration] of the application.
 */
fun Application.configureVision() {
    val graph = Graph()
    val data = TensorflowBookDetector::class.java.getResourceAsStream("/tensorflow/inception-book-model.pb")
        .use { it.readBytes() }
    graph.importGraphDef(data)

    // Define the vision configuration
    // TODO Implement a way to make this configuration configurable via the application.conf file
    VisionConfiguration(
        detector = TensorflowBookDetector(graph),
        extractor = GoogleVisionTextExtractor(ImageAnnotatorClient.create()),
        catalogue = attributes[CatalogueConfiguration.KEY]
    ).also {
        attributes.put(VisionConfiguration.KEY, it)
    }
}
