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

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.auth.oauth2.scoped
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.locations.Location
import io.ktor.locations.locations
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.application
import io.ktor.routing.post
import io.ktor.routing.route
import nl.tudelft.booklab.backend.baseUrl
import nl.tudelft.booklab.backend.services.password.PasswordService
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import nl.tudelft.booklab.backend.services.user.UserServiceException
import nl.tudelft.booklab.backend.spring.inject
import io.ktor.locations.get as getLocation

/**
 * Define user endpoints at the current route of the REST API.
 */
fun Route.users() {
    scoped("user:registration") { userCreate() }
    scoped("user:profile") { userResource() }
}

/**
 * Define the endpoint to register a user.
 */
internal fun Route.userCreate() {
    val users: UserService = application.inject()
    val passwordService: PasswordService = application.inject()

    val post = {
        post {
            val registration: UserRegistrationRequest? = try { call.receive() } catch (e: Exception) { null }

            if (registration == null) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest("The body of the request is invalid."))
                return@post
            }

            try {
                val user = users.save(User(0, registration.email, passwordService.hash(registration.password)))
                val location = URLBuilder().run {
                    takeFrom(this@userCreate.application.baseUrl)
                    encodedPath = call.request.uri.removeSurrounding("/") + call.locations.href(UserProfileRoute(user))
                    buildString()
                }
                call.response.header("Location", location)
                call.respond(HttpStatusCode.Created, Success(user))
            } catch (e: UserServiceException.InvalidInformationException) {
                call.respond(HttpStatusCode.BadRequest, InvalidRequest(e.message))
            } catch (e: UserServiceException.UserAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, ResourceAlreadyExists(e.message))
            }
        }
    }

    accept(ContentType.Application.Json) { post() }
}

/**
 * This class defines the shape of a registration request that is sent by the client.
 *
 * @property email The email of the user to be registered.
 * @property password The password of the user to be registered.
 */
data class UserRegistrationRequest(val email: String, val password: String)

/**
 * Define the endpoint to view the profile of a user.
 */
internal fun Route.userResource() {
    val userService: UserService = application.inject()

    route("/{user}") {
        intercept(ApplicationCallPipeline.Call) {
            val param = call.parameters["user"]!!
            val id = param.toIntOrNull()

            if (id == null || !userService.existsById(id)) {
                call.respond(HttpStatusCode.NotFound, NotFound("The user '$param' was not found on the server."))
                finish()
            }
        }
    }
    getLocation<UserProfileRoute> { (user) ->
        call.respond(Success(user))
    }
    route("/{user}") {
        handle { call.respond(HttpStatusCode.MethodNotAllowed, MethodNotAllowed()) }
    }
}

/**
 * A route to a user profile.
 *
 * @property user The user associated with the route or `null`.
 */
@Location("/{user}")
data class UserProfileRoute(val user: User?)
