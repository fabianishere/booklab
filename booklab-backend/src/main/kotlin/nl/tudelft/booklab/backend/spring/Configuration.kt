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

package nl.tudelft.booklab.backend.spring

import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue
import org.springframework.beans.factory.FactoryBean
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.PropertySource

/**
 * A [PropertySource] for a Spring Container for Ktor applications.
 *
 * @param name The name of the property source.
 * @param source The [ApplicationConfig] to use for the configuration.
 */
class KtorPropertySource(name: String, source: ApplicationConfig) : PropertySource<ApplicationConfig>(name, source) {
    override fun getProperty(path: String): Any? {
        try {
            return source.config(path)
        } catch (e: Exception) {}
        try {
            return source.configList(path)
        } catch (e: Exception) {}
        try {
            return source.property(path)
        } catch (e: Exception) {}
        return null
    }
}

/**
 * A [Converter] that converts an instance of [ApplicationConfigValue] to a string.
 */
object KtorApplicationConfigValueStringConverter : Converter<ApplicationConfigValue, String> {
    override fun convert(source: ApplicationConfigValue): String? = source.getString()
}

/**
 * A [Converter] that converts an instance of [ApplicationConfigValue] to a list of strings.
 */
object KtorApplicationConfigValueListConverter : Converter<ApplicationConfigValue, List<String>> {
    override fun convert(source: ApplicationConfigValue): List<String> = source.getList()
}

/**
 * A [FactoryBean] for accessing an [ApplicationConfig] object of the running [Application].
 *
 * @property application The Ktor [Application] to read the configuration from.
 * @property path The path to the object.
 */
class KtorApplicationConfigFactoryBean(
    private val application: Application,
    private val path: String? = null
) : FactoryBean<ApplicationConfig> {

    override fun getObject(): ApplicationConfig = application.environment.config.let {
        if (path != null)
            it.config(path)
        else
            it
    }

    override fun getObjectType(): Class<*> = ApplicationConfig::class.java
}
