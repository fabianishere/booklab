/*
 * Copyright 2018 Fabian Mastenbroek.
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

import io.ktor.auth.Credential
import io.ktor.auth.Principal
import io.ktor.auth.oauth2.grant.GrantHandler
import io.ktor.config.ApplicationConfig
import io.ktor.util.decodeBase64
import java.net.URI
import java.security.MessageDigest

/**
 * The client credentials which is used to authenticate a client principal.
 *
 * @property id The unique identifier of the client.
 * @property secret The optional secret of the client.
 */
data class ClientCredential(val id: String, val secret: String?) : Credential

/**
 * The client principal that has been authenticated.
 *
 * @property id The unique identifier of the client.
 * @property redirectUri The redirect uri of the client.
 * @property scopes The scopes of the client.
 */
data class ClientIdPrincipal(
    val id: String,
    val redirectUri: URI? = null,
    val scopes: Set<String> = emptySet()
) : Principal

/**
 * A [PrincipalRepository] for authenticating by [ClientCredential]s.
 */
interface ClientRepository<C : Principal> : PrincipalRepository<ClientCredential, C> {
    /**
     * Validate the given grant handler for the given client principal.
     *
     * @param client The client principal to validate for.
     * @param handler The handler to validate.
     * @return `true` if the grant handler is allowed, `false` otherwise.
     */
    suspend fun validateGrantHandler(client: C, handler: GrantHandler<C, *>): Boolean = true

    /**
     * Validate the redirect uri for the given client principal.
     *
     * @param client The client principal to validate for.
     * @param redirectUri The redirect uri to validate.
     * @return `true` if the redirect uri is valid, `false` otherwise.
     */
    suspend fun validateRedirectUri(client: C, redirectUri: URI?): Boolean = true

    /**
     * Validate the given scope for a client principal.
     *
     * @param client The client principal to validate for.
     * @param scope The scope uri to validate.
     * @return `true` if the scope is valid, `false` otherwise.
     */
    suspend fun validateScope(client: C, scope: String?): Boolean = true
}

/**
 * A [ClientRepository] that authenticates [ClientIdPrincipal]s based on a hash table.
 *
 * @param digester A function to convert the secret of a client into a message digest.
 * @param table The table of clients.
 */
class ClientHashedTableRepository(
    val digester: (String) -> ByteArray,
    val table: Map<String, Pair<ClientIdPrincipal, ByteArray>>
) : ClientRepository<ClientIdPrincipal> {
    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override val ClientIdPrincipal.id: String get() = id

    override suspend fun lookup(id: String): ClientIdPrincipal? {
        val (principal, _) = table[id] ?: return null
        return principal
    }

    override suspend fun validate(credential: ClientCredential, authorize: Boolean): ClientIdPrincipal? {
        val (principal, hash) = table[credential.id] ?: return null

        if (!authorize && (credential.secret == null || !MessageDigest.isEqual(digester(credential.secret), hash))) {
            // The client secret does not match
            return null
        }

        return principal
    }

    override suspend fun validateRedirectUri(client: ClientIdPrincipal, redirectUri: URI?): Boolean {
        return client.redirectUri == null || client.redirectUri == redirectUri
    }

    override suspend fun validateScope(client: ClientIdPrincipal, scope: String?): Boolean {
        return scope in client.scopes
    }
}

/**
 * Parse a map of clients from the application configuration.
 */
fun ApplicationConfig.parseClients(name: String = "clients"): Map<String, Pair<ClientIdPrincipal, ByteArray>> =
    configList(name)
        .map {
            val id = it.property("id").getString()
            val redirectUri = it.propertyOrNull("redirect_uri")?.let { URI(it.getString()) }
            val scopes = it.propertyOrNull("scopes")?.getList()?.toHashSet() ?: emptySet<String>()
            val hash = decodeBase64(it.property("hash").getString())
            val principal = ClientIdPrincipal(id, redirectUri, scopes)
            id to (principal to hash)
        }
        .toMap()
