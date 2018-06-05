/*
 * Copyright 2018 The BookLab Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.tudelft.booklab.recommender.rating.goodreads

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream

/**
 * a singleton parser that parses JSON results from
 * the Goodreads database
 */
class GoodreadsParser(private val mapper: ObjectMapper = jacksonObjectMapper()) {

    /**
     * the parse function
     *
     * @param stream is the [InputStream] returned from Goodreads
     * @return a [Results] object
     */
    fun parse(stream: InputStream): Results {
        return mapper.readValue(stream, Results::class.java)
    }
}
