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

import io.ktor.auth.oauth2.repository.ClientHashedTableRepository
import io.ktor.auth.oauth2.repository.parseClients
import io.ktor.config.ApplicationConfig
import io.ktor.util.getDigestFunction
import org.springframework.beans.factory.FactoryBean
import java.util.function.Function

/**
 * A function that digests the given string.
 */
typealias DigestFunction = (String) -> ByteArray

/**
 * A [FactoryBean] for constructing a [DigestFunction] which is consumed by a [ClientHashedTableRepository].
 *
 * @property algorithm The algorithm to create a [DigestFunction] for.
 * @property salt The salt to use.
 */
class DigesterFunctionFactoryBean(val algorithm: String, val salt: String) : FactoryBean<DigestFunction> {
    override fun getObject(): DigestFunction = getDigestFunction(algorithm, salt)

    override fun getObjectType(): Class<*> = Function::class.java
}

/**
 * A [FactoryBean] for constructing a [ClientHashedTableRepository] from the Ktor application configuration.
 *
 * @property config The application configuration to construct the repository from.
 * @property name The name of the list to use.
 * @property digester The digester to use.
 */
class ClientHashedTableRepositoryFactoryBean(
    private val config: ApplicationConfig,
    private val name: String = "clients",
    private val digester: DigestFunction
) : FactoryBean<ClientHashedTableRepository> {

    override fun getObject(): ClientHashedTableRepository {
        return ClientHashedTableRepository(
            digester = digester,
            table = config.parseClients(name)
        )
    }

    override fun getObjectType(): Class<*> = ClientHashedTableRepository::class.java
}
