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
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.registerBean

/**
 * Create a [GenericApplicationContext] for the application.
 */
fun createApplicationContext(): GenericApplicationContext {
    val context = AnnotationConfigApplicationContext()
    val configuration = BooklabSpringConfiguration()
    context.registerBean { configuration }
    context.register(BooklabSpringConfiguration::class.java)
    configuration.beans().initialize(context)
    return context
}

/**
 * A bootstrap method for a Ktor [Application] which runs a [BooklabApplication] inside a Spring DI container.
 */
fun Application.bootstrap() {
    val context = createApplicationContext()
    context.bootstrap(this, Application::booklab)
}
