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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

/**
 * A Jackson [StdDeserializer] for deserializing publish dates of books.
 */
class JacksonPublishDateDeserializer : StdDeserializer<TemporalAccessor>(TemporalAccessor::class.java) {
    /**
     * The [DateTimeFormatter] to use to parse the date.
     */
    private val formatter = DateTimeFormatter.ofPattern("yyyy[-MM[-dd]]")

    /**
     * The [TemporalQuery]s to use to parse the different formats.
     */
    private val queries = arrayOf(TemporalQuery { LocalDate.from(it) },
        TemporalQuery { YearMonth.from(it) },
        TemporalQuery { Year.from(it) })

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TemporalAccessor {
        val node: JsonNode = p.codec.readTree(p)
        return formatter.parseBest(node.asText(), *queries)
    }
}
