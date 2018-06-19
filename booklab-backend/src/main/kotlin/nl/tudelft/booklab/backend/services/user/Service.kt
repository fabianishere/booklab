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

package nl.tudelft.booklab.backend.services.user

import org.springframework.transaction.annotation.Transactional
import javax.validation.ConstraintViolationException

/**
 * A service for managing users.
 */
class UserService(private val repository: UserRepository) {
    /**
     * Find a user by its identifier.
     *
     * @return The user that has been found or `null`.
     */
    @Transactional
    fun findById(id: Int): User? = repository.findById(id).orElse(null)

    /**
     * Find a user by its email address.
     *
     * @return The user that has been found or `null`.
     */
    @Transactional
    fun findByEmail(email: String): User? = repository.findByEmail(email)

    /**
     * Determine whether a user exists by its identifier.
     *
     * @param id The identifier of the user to find.
     * @return `true` if the user exists, `false` otherwise.
     */
    @Transactional
    fun existsById(id: Int): Boolean = repository.existsById(id)

    /**
     * Register the given user to the specified repository.
     *
     * @param user The user to register.
     * @return The user that has been registered.
     */
    @Transactional
    @Throws(UserServiceException::class)
    fun save(user: User): User {
        if (repository.existsByEmail(user.email)) {
            throw UserServiceException.UserAlreadyExistsException("There exists already a user with email ${user.email}.")
        }
        try {
            return repository.save(user)
        } catch (e: ConstraintViolationException) {
            throw UserServiceException.InvalidInformationException(e.message ?: "A constraint violation occurred")
        }
    }
}

/**
 * An exception that is possibly thrown by a method of the [UserService] class.
 */
sealed class UserServiceException(description: String) : Exception(description) {
    /**
     * The given information to register the user was invalid.
     */
    class InvalidInformationException(description: String) : UserServiceException(description)

    /**
     * This [UserServiceException] is thrown when the user already exists.
     */
    class UserAlreadyExistsException(description: String) : UserServiceException(description)
}
