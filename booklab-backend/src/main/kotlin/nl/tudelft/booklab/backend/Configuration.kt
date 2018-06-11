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
import io.ktor.routing.Routing
import io.ktor.util.getDigestFunction
import nl.tudelft.booklab.backend.services.auth.JwtService
import nl.tudelft.booklab.backend.services.vision.VisionService
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.beans
import org.springframework.core.env.get
import java.time.Duration

/**
 * The configuration for the Spring Container that we use around the Ktor [Application].
 */
@Configuration
@ComponentScan("nl.tudelft.booklab.backend")
class BooklabSpringConfiguration {
    /**
     * Describe the beans to be registered in the Spring container.
     */
    fun beans(): BeanDefinitionDsl = beans {
        // Routes
        bean<ApplicationRoutes> {
            Routing::routes
        }

        // Vision
        bean(isLazyInit = true) { VisionService(ref("detector"), ref("extractor"), ref("catalogue")) }

        auth()
    }

    /**
     * This method defines the beans related to the OAuth 2.0 authorization mechanism.
     */
    fun BeanDefinitionDsl.auth() {
        // JwtService
        bean {
            val issuer = env["auth.jwt.domain"]
            val audience = env["auth.jwt.audience"]
            val realm = env["auth.jwt.realm"]
            val validity = Duration.parse(env["auth.jwt.validity"])
            val passphrase = env["auth.jwt.passphrase"]

            JwtService(issuer, audience, realm, validity, Algorithm.HMAC512(passphrase))
        }

        bean("oauth:repository:client", isLazyInit = true) {
            // TODO Remove reliance on the application for configuring the clients
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
    }
}
