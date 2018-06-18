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

package nl.tudelft.booklab.backend.services.catalogue

import io.ktor.util.ConversionService
import io.ktor.util.DataConversionException
import kotlinx.coroutines.experimental.runBlocking
import nl.tudelft.booklab.backend.ktor.TypedConversionService
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * A [ConversionService] for books in the catalogue in the Ktor application.
 *
 * @property service The [CatalogueService] to use for looking up books.
 */
class CatalogueConversionService(private val service: CatalogueService) : TypedConversionService {
    override val types: List<KClass<*>> = listOf(Book::class)

    override fun fromValues(values: List<String>, type: Type): Any? {
        return values.singleOrNull()?.let {
            runBlocking { service.findById(it) }
        }
    }

    override fun toValues(value: Any?): List<String> {
        return when (value) {
            null -> listOf()
            is Book -> listOf(value.identifiers.values.first())
            else -> throw DataConversionException("Cannot convert $value as Book")
        }
    }
}
