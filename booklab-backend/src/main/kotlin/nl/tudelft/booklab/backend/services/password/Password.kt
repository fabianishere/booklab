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

package nl.tudelft.booklab.backend.services.password

/**
 * An interface for a class that manages passwords by hashing them and verifying whether existing passwords match.
 */
interface PasswordService {
    /**
     * Hash the given password and return the result.
     *
     * @return The password that has been hashed.
     */
    fun hash(password: String): String

    /**
     * Verify whether the given password matches the hashed password of a user.
     *
     * @param password The password to verify.
     * @param hash The hash to verify the password with.
     * @return `true` if the password matches, `false` otherwise.
     */
    fun verify(password: String, hash: String): Boolean
}
