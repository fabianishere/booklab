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
import io.ktor.application.call
import io.ktor.auth.oauth2.scoped
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.application
import io.ktor.routing.post
import nl.tudelft.booklab.backend.services.user.PasswordService
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import nl.tudelft.booklab.backend.services.user.UserServiceException
import nl.tudelft.booklab.backend.spring.inject

/**
 * Define user endpoints at the current route of the REST API.
 */
fun Route.users() {
    scoped("user:registration") { register() }
}

/**
 * Define the endpoint to register a user.
 */
internal fun Route.register() {
    val users: UserService = application.inject()
    val passwordService: PasswordService = application.inject()

    fun Route.handle() {
        post {
            val registration: UserRegistrationRequest? = try { call.receive() } catch (e: Exception) { null }

            if (registration == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    UserRegistrationFailure("invalid_request", "The body of the request is invalid.")
                )
                return@post
            }

            try {
                val user = users.save(User(0, registration.email, passwordService.hash(registration.password)))
                call.respond(UserRegistrationSuccess(user.id, user.email))
            } catch (e: UserServiceException.InvalidUserInformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    UserRegistrationFailure("invalid_request", e.message)
                )
            } catch (e: UserServiceException.UserAlreadyExistsException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    UserRegistrationFailure("invalid_request", e.message)
                )
            }
        }
    }

    accept(ContentType.Application.Json) { handle() }
}

/**
 * This class defines the shape of a registration request that is sent by the client.
 *
 * @property email The email of the user to be registered.
 * @property password The password of the user to be registered.
 */
data class UserRegistrationRequest(val email: String, val password: String)

/**
 * This class defines the shape of a registration response that is sent to the client.
 *
 * @param id The unique identifier of the user.
 * @param email The email address of the user.
 */
data class UserRegistrationSuccess(val id: Int, val email: String)

/**
 * This class defines the shape of an error that occurred during the registration of a user.
 */
data class UserRegistrationFailure(
    @JsonProperty("error")
    val type: String,
    @JsonProperty("error_description")
    val description: String?
)
