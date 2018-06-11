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
import nl.tudelft.booklab.backend.spring.bootstrap
import nl.tudelft.booklab.backend.spring.configure
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.registerBean

/**
 * Create a [GenericApplicationContext] for the application.
 */
fun createApplicationContext(application: Application): GenericApplicationContext {
    val context = AnnotationConfigApplicationContext()

    // Configure the container for the given application
    context.configure(application)

    // Run the compile time bean configuration
    context.configureStatic()

    // The cast will always succeed since our HOCON configuration only returns string lists.
    @Suppress("UNCHECKED_CAST")
    val contexts = context.environment.getProperty("spring.contexts", List::class.java) as? List<String>
    if (contexts != null) {
        context.configureDynamic(contexts)
    }

    return context
}

/**
 * Load the static bean configuration.
 */
internal fun AnnotationConfigApplicationContext.configureStatic() {
    val configuration = BooklabSpringConfiguration()
    registerBean { configuration }
    register(BooklabSpringConfiguration::class.java)
    configuration.beans().initialize(this)
}

/**
 * Load the dynamic bean configuration.
 *
 * @param paths The paths to the dynamic Spring configuration files.
 */
internal fun AnnotationConfigApplicationContext.configureDynamic(paths: List<String>) {
    val reader = XmlBeanDefinitionReader(this)
    paths.forEach { reader.loadBeanDefinitions(it) }
}

/**
 * A bootstrap method for a Ktor [Application] which runs a Booklab application inside a Spring DI container.
 */
fun Application.bootstrap() = createApplicationContext(this).bootstrap(this, Application::booklab)
