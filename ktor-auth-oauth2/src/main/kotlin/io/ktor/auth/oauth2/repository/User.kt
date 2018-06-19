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

package io.ktor.auth.oauth2.repository

import io.ktor.auth.Principal
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.config.ApplicationConfig
import io.ktor.util.decodeBase64
import java.security.MessageDigest

/**
 * A [PrincipalRepository] for authenticating by [UserPasswordCredential]s.
 */
interface UserRepository<C : Principal> : PrincipalRepository<UserPasswordCredential, C>

/**
 * A [UserRepository] that authenticates [UserIdPrincipal]s based on a hash table.
 *
 * @param digester A function to convert the secret of a client into a message digest.
 * @param table The table of clients.
 */
class UserHashedTableRepository(
    val digester: (String) -> ByteArray,
    val table: Map<String, ByteArray>
) : UserRepository<UserIdPrincipal> {
    override val UserIdPrincipal.id: String get() = name

    override suspend fun lookup(id: String): UserIdPrincipal? = UserIdPrincipal(id).takeIf { table.containsKey(id) }

    override suspend fun validate(credential: UserPasswordCredential, authorize: Boolean): UserIdPrincipal? {
        val hash = table[credential.name] ?: return null

        if (!(authorize || MessageDigest.isEqual(digester(credential.password), hash))) {
            // The user password does not match
            return null
        }

        return UserIdPrincipal(credential.name)
    }
}

/**
 * Parse a map of users from the application configuration.
 */
fun ApplicationConfig.parseUsers(name: String = "users") =
    configList(name)
        .map { it.property("name").getString() to decodeBase64(it.property("hash").getString()) }
        .toMap()
