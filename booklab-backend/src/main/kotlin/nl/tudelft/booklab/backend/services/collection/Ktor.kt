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

package nl.tudelft.booklab.backend.services.collection

import io.ktor.util.ConversionService
import io.ktor.util.DataConversionException
import nl.tudelft.booklab.backend.ktor.TypedConversionService
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * A [ConversionService] for book collections in the Ktor application.
 *
 * @property service The [BookCollectionService] to use for looking up collections.
 */
class BookCollectionConversionService(private val service: BookCollectionService) : TypedConversionService {
    override val types: List<KClass<*>> = listOf(BookCollection::class)

    override fun fromValues(values: List<String>, type: Type): Any? {
        return values.singleOrNull()?.toIntOrNull()?.let { service.findById(it) }
    }

    override fun toValues(value: Any?): List<String> {
        return when (value) {
            null -> listOf()
            is BookCollection -> listOf(value.id.toString())
            else -> throw DataConversionException("Cannot convert $value as BookCollection")
        }
    }
}
