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

import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.ApplicationEnvironment
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.withApplication
import nl.tudelft.booklab.backend.spring.configure
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

/**
 * Construct a [TestApplicationEngine] from a configuration file and run the given block in its scope.
 *
 * @param module The Ktor module to run in test.
 * @param test The block to run in its scope.
 */
fun <R> withTestEngine(module: Application.() -> Unit, test: TestApplicationEngine.() -> R) =
    withApplication(createTestEnvironment(module), test = test)

/**
 * Construct a [ApplicationEnvironment] for the given module.
 *
 * @param module The Ktor module to run in test.
 */
fun createTestEnvironment(module: Application.() -> Unit): ApplicationEngineEnvironment = createTestEnvironment {
    config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
    module { module() }
}

/**
 * Create a [GenericApplicationContext] for testing purposes.
 *
 * @param builder The builder to create the context.
 * @return The created context.
 */
fun Application.createTestContext(builder: GenericApplicationContext.() -> Unit = {}): GenericApplicationContext {
    val root = GenericApplicationContext {
        beans {
            auth()
        }.initialize(this)
    }
    root.refresh()
    root.configure(this)
    return GenericApplicationContext(root).apply {
        configure(this@createTestContext)
        builder()
    }
}
