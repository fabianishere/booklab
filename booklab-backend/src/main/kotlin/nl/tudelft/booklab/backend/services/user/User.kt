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

import io.ktor.auth.Principal
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.Entity

/**
 * An entity representing a user account.
 *
 * @property id The identifier of the user.
 * @property email The email address of the user.
 * @property password A hashed password associated with the user.
 */
@Entity
data class User(
    val id: Int,
    val email: String,
    val password: String
) : Principal

/**
 * A service for managing users.
 */
class UserService(private val repository: UserRepository) {
    /**
     * Find a user by its email address.
     *
     * @return The user that has been found or `null`.
     */
    @Transactional
    fun findByEmail(email: String): User? = repository.findByEmail(email)

    /**
     * Register the given user to the specified repository.
     *
     * @param user The user to register.
     * @return The user that has been registered.
     */
    @Transactional
    @Throws(UserServiceException::class)
    fun save(user: User): User {
        // TODO validate whether email is correct
        if (user.email.isBlank()) {
            throw UserServiceException.InvalidUserInformationException("No valid email address given")
        } else if (user.password.isEmpty()) {
            throw UserServiceException.InvalidUserInformationException("No password given")
        } else if (repository.existsByEmail(user.email)) {
            throw UserServiceException.UserAlreadyExistsException("There exists already a user with email ${user.email}.")
        }
        return repository.save(user)
    }
}

/**
 * An exception that is possibly thrown by a method of the [UserService] class.
 */
sealed class UserServiceException(description: String) : Exception(description) {
    /**
     * The given information to register the user was invalid.
     */
    class InvalidUserInformationException(description: String) : UserServiceException(description)

    /**
     * This [UserServiceException] is thrown when the user already exists.
     */
    class UserAlreadyExistsException(description: String) : UserServiceException(description)
}

/**
 * A repository for accessing users from a database.
 */
interface UserRepository : CrudRepository<User, Int> {
    /**
     * Find a user by its email address.
     *
     * @return The user that has been found or `null`.
     */
    fun findByEmail(email: String): User?

    /**
     * Determine whether the given user exists by email.
     */
    fun existsByEmail(email: String): Boolean
}
