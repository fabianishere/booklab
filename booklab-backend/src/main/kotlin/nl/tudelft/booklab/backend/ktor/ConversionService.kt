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

package nl.tudelft.booklab.backend.ktor

import io.ktor.util.ConversionService
import kotlin.reflect.KClass

/**
 * A [ConversionService] that specifies which types it can convert.
 */
interface TypedConversionService : ConversionService {
    /**
     * The list of types that can be converted.
     */
    val types: List<KClass<*>>
}
