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

import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.util.AttributeKey
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

/**
 * The [AttributeKey] for the application's [EntityManagerFactory].
 */
val JPA_KEY = AttributeKey<EntityManagerFactory>("Jpa")

/**
 * Construct an [EntityManagerFactory] from the following [ApplicationConfiguration] instance.
 */
fun ApplicationConfig.asEntityManagerFactory(): EntityManagerFactory {
    val name = property("persistence-unit").getString()
    val properties = mapOf(
        "javax.persistence.jdbc.url" to propertyOrNull("connection.url")?.getString(),
        "javax.persistence.jdbc.user" to propertyOrNull("connection.user")?.getString(),
        "javax.persistence.jdbc.password" to propertyOrNull("connection.password")?.getString()
    )
    return Persistence.createEntityManagerFactory(name, properties)
}

/**
 * Return the [EntityManagerFactory] of this application.
 *
 * @return The [EntityManagerFactory] of this application.
 */
val Application.entityManagerFactory: EntityManagerFactory get() = attributes[JPA_KEY]

/**
 * Run the given block in a transaction, committing on return of the block.
 *
 * @param block The block to execute in the transaction.
 * @return The value returned from the block.
 */

fun <T> EntityManager.transaction(block: () -> T): T {
    transaction.begin()
    val res = block()
    transaction.commit()
    return res
}
