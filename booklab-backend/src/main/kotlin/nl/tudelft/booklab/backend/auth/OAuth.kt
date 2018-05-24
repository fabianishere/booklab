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

package nl.tudelft.booklab.backend.auth

import io.ktor.application.Application
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.oauth2.OAuthServer
import io.ktor.auth.oauth2.grant.ClientCredentialsGrantHandler
import io.ktor.auth.oauth2.grant.PasswordGrantHandler
import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.ClientRepository
import io.ktor.auth.oauth2.repository.JwtAccessTokenRepository
import io.ktor.auth.oauth2.repository.UserHashedTableRepository
import io.ktor.auth.oauth2.repository.UserRepository
import io.ktor.auth.oauth2.repository.parseClients
import io.ktor.auth.oauth2.repository.parseUsers
import io.ktor.config.ApplicationConfig
import io.ktor.util.AttributeKey
import io.ktor.util.getDigestFunction
import io.ktor.auth.oauth2.repository.JwtConfiguration as JwtOAuthConfiguration

/**
 * The configuration for the OAuth authorization server.
 *
 * @property clientRepository The repository for looking up and validating clients.
 * @property userRepository The repository for looking up and validating users.
 * @property jwt The [JwtConfiguration] for generating JWT tokens.
 */
data class OAuthConfiguration(
    val clientRepository: ClientRepository<ClientIdPrincipal>,
    val userRepository: UserRepository<UserIdPrincipal>,
    val jwt: JwtConfiguration
) {
    /**
     * The [OAuthServer] object.
     */
    val server = OAuthServer(
        handlers = mapOf(
            "password" to PasswordGrantHandler(userRepository),
            "client_credentials" to ClientCredentialsGrantHandler()
        ),
        clientRepository = clientRepository,
        tokenRepository = JwtAccessTokenRepository(
            configuration = JwtOAuthConfiguration(jwt.issuer, jwt.audience, jwt.algorithm),
            userRepository = userRepository,
            clientRepository = clientRepository,
            validity = jwt.validity
        )
    )
    companion object {
        /**
         * The attribute key that allows the user to access the [OAuthConfiguration] object within an application.
         */
        val KEY = AttributeKey<OAuthConfiguration>("OAuthConfiguration")
    }
}

/**
 * Build the [OAuthConfiguration] object for the given configuration environment.
 *
 * @return The [OAuthConfiguration] instance that has been built.
 */
fun ApplicationConfig.asOAuthConfiguration(): OAuthConfiguration {
    val jwt = config("jwt").asJwtConfiguration()
    val clientRepository = ClientHashedTableRepository(
        digester = getDigestFunction("SHA-256", salt = "ktor"),
        table = parseClients()
    )
    val userRepository = UserHashedTableRepository(
        digester = getDigestFunction("SHA-256", salt = "ktor"),
        table = parseUsers()
    )
    return OAuthConfiguration(clientRepository, userRepository, jwt)
}

/**
 * Extension method for accessing the [OAuthConfiguration] instance of the [Application].
 */
val Application.oauth: OAuthConfiguration get() = attributes[OAuthConfiguration.KEY]
