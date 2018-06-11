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

package nl.tudelft.booklab.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.auth.oauth2.grant.ClientCredentialsGrantHandler
import io.ktor.auth.oauth2.grant.PasswordGrantHandler
import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.ClientIdPrincipal
import io.ktor.auth.oauth2.repository.JwtAccessTokenRepository
import io.ktor.auth.oauth2.repository.JwtConfiguration
import io.ktor.auth.oauth2.repository.UserHashedTableRepository
import io.ktor.auth.oauth2.repository.parseClients
import io.ktor.auth.oauth2.repository.parseUsers
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.util.getDigestFunction
import nl.tudelft.booklab.backend.services.auth.BooklabOAuthServer
import nl.tudelft.booklab.backend.services.user.User
import nl.tudelft.booklab.backend.spring.inject
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import java.time.Duration
import java.time.Instant
import java.util.Date

/**
 * Define default authorization related beans for the Spring container.
 */
fun BeanDefinitionDsl.auth() {
    bean("oauth:repository:token", isLazyInit = true) {
        val issuer = env["auth.jwt.domain"]
        val audience = env["auth.jwt.audience"]
        val validity = Duration.parse(env["auth.jwt.validity"])
        val passphrase = env["auth.jwt.passphrase"]

        JwtAccessTokenRepository<ClientIdPrincipal, User>(
            JwtConfiguration(issuer, audience, Algorithm.HMAC512(passphrase)),
            userRepository = ref("oauth:repository:user"),
            clientRepository = ref("oauth:repository:client"),
            validity = validity
        )
    }
    bean("oauth:repository:client", isLazyInit = true) {
        val application: Application = ref()
        ClientHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = application.environment.config.config("auth").parseClients()
        )
    }
    bean("oauth:repository:user", isLazyInit = true) {
        val application: Application = ref()
        UserHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = application.environment.config.config("auth").parseUsers()
        )
    }

    // OAuthService
    bean("oauth:server", isLazyInit = true) {
        BooklabOAuthServer(
            handlers = mapOf(
                "password" to PasswordGrantHandler(ref("oauth:repository:user")),
                "client_credentials" to ClientCredentialsGrantHandler()
            ),
            clientRepository = ref(),
            tokenRepository = ref()
        )
    }
}

/**
 * Configure the authorization header for calls that require an access token.
 */
fun TestApplicationRequest.configureAuthorization(id: String, scopes: List<String> = emptyList()) {
    val env = call.application.inject<Environment>()
    val token = let {
        val issuer = env["auth.jwt.domain"]
        val audience = env["auth.jwt.audience"]
        val validity = Duration.parse(env["auth.jwt.validity"])
        val passphrase = env["auth.jwt.passphrase"]

        val now = Instant.now()

        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject("access-token")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(Instant.now().plus(validity)))
            .withClaim("user", "test@example.com")
            .withClaim("client", id)
            .withArrayClaim("scopes", scopes.toTypedArray())
            .sign(Algorithm.HMAC512(passphrase))
    }

    addHeader(HttpHeaders.Authorization, "Bearer $token")
}
