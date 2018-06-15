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

package nl.tudelft.booklab.backend.services.user

import io.ktor.util.ConversionService
import io.ktor.util.DataConversionException
import nl.tudelft.booklab.backend.ktor.TypedConversionService
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * A [ConversionService] for users in the Ktor application.
 *
 * @property service The [UserService] to use for looking up users.
 */
class UserConversionService(private val service: UserService) : TypedConversionService {
    override val types: List<KClass<*>> = listOf(User::class)

    override fun fromValues(values: List<String>, type: Type): Any? {
        return values.singleOrNull()?.toIntOrNull()?.let { service.findById(it) }
    }

    override fun toValues(value: Any?): List<String> {
        return when (value) {
            null -> listOf()
            is User -> listOf(value.id.toString())
            else -> throw DataConversionException("Cannot convert $value as User")
        }
    }
}
