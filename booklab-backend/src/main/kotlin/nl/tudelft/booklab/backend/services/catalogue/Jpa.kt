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

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
import javax.persistence.AttributeConverter

/**
 * An [AttributeConverter] for converting the publish date to a database column and back.
 */
class JpaPublishDateConverter : AttributeConverter<TemporalAccessor, String> {
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

    override fun convertToDatabaseColumn(attribute: TemporalAccessor?): String? = attribute?.toString()

    override fun convertToEntityAttribute(dbData: String?): TemporalAccessor? = dbData?.let { formatter.parseBest(it, *queries) }
}
