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

import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.oauth2.OAuthServer
import io.ktor.auth.oauth2.grant.ClientCredentialsGrantHandler
import io.ktor.auth.oauth2.grant.PasswordGrantHandler
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.ClientRepository
import io.ktor.auth.oauth2.repository.JwtAccessTokenRepository
import io.ktor.auth.oauth2.repository.UserRepository
import io.ktor.auth.oauth2.repository.JwtConfiguration as JwtOAuthConfiguration

/**
 * A service for the OAuth authorization server.
 *
 * @property clientRepository The repository for looking up and validating clients.
 * @property userRepository The repository for looking up and validating users.
 * @property jwt The [JwtService] for generating JWT tokens.
 */
data class OAuthService(
    val clientRepository: ClientRepository<ClientIdPrincipal>,
    val userRepository: UserRepository<UserIdPrincipal>,
    val jwt: JwtService
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
}
