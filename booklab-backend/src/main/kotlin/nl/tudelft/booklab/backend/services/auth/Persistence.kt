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

package nl.tudelft.booklab.backend.services.auth

import io.ktor.auth.UserPasswordCredential
import nl.tudelft.booklab.backend.services.user.PasswordService
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.services.user.UserService
import io.ktor.auth.oauth2.repository.UserRepository as OAuthUserRepository

/**
 * An [OAuthUserRepository] that uses the user services of this module for looking up users.
 *
 * @property userService The service to use for looking up users.
 * @property passwordService The service to verify passwords.
*/
class PersistentUserRepository(
    private val userService: UserService,
    private val passwordService: PasswordService
) : OAuthUserRepository<User> {
    /**
     * A unique string identifier of the user.
     */
    override val User.id: String get() = email

    /**
     * Look up a user in the database.
     */
    override suspend fun lookup(id: String): User? = userService.findByEmail(id)

    override suspend fun validate(credential: UserPasswordCredential, authorize: Boolean): User? {
        return lookup(credential.name)?.takeIf {
            authorize || passwordService.verify(credential.password, it.password)
        }
    }
}
