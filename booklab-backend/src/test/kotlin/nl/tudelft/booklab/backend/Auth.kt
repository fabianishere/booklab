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

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.UserHashedTableRepository
import io.ktor.auth.oauth2.repository.parseClients
import io.ktor.auth.oauth2.repository.parseUsers
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.util.getDigestFunction
import nl.tudelft.booklab.backend.services.auth.JwtService
import nl.tudelft.booklab.backend.services.auth.OAuthService
import nl.tudelft.booklab.backend.spring.inject
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.core.env.get
import java.time.Duration
import java.time.Instant
import java.util.Date

/**
 * Define default authorization related beans for the Spring container.
 */
fun BeanDefinitionDsl.auth() {
    // JwtService
    bean(isLazyInit = true) {
        val issuer = env["auth.jwt.domain"]
        val audience = env["auth.jwt.audience"]
        val realm = env["auth.jwt.realm"]
        val validity = Duration.parse(env["auth.jwt.validity"])
        val passphrase = env["auth.jwt.passphrase"]

        JwtService(issuer, audience, realm, validity, Algorithm.HMAC512(passphrase))
    }

    // OAuthService
    bean(isLazyInit = true) {
        // TODO Remove reliance on the application for configuring the clients
        val application: Application = ref()
        val clients = ClientHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = application.environment.config.config("auth").parseClients()
        )
        val users = UserHashedTableRepository(
            digester = getDigestFunction("SHA-256", salt = "ktor"),
            table = application.environment.config.config("auth").parseUsers()
        )
        OAuthService(clients, users, ref())
    }
}

/**
 * Configure the authorization header for calls that require an access token.
 */
fun TestApplicationRequest.configureAuthorization(id: String, scopes: List<String> = emptyList()) {
    val token = call.application.inject<JwtService>().run {
        val now = Instant.now()

        creator
            .withSubject("access-token")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(Instant.now().plus(validity)))
            .withClaim("user", "test@example.com")
            .withClaim("client", id)
            .withArrayClaim("scopes", scopes.toTypedArray())
            .sign(algorithm)
    }

    addHeader(HttpHeaders.Authorization, "Bearer $token")
}
