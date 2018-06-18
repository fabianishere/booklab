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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.catalogue.Ratings
import java.net.URL
import java.time.temporal.TemporalAccessor
import javax.persistence.Entity
import nl.tudelft.booklab.catalogue.Book as AbstractBook

/**
 * A domain model class representing a book.
 *
 * @property id The internal identifier of the book in the database.
 * @property identifiers A map containing the identifiers of the book.
 * @property title The main title of the book.
 * @property subtitle The subtitle of the book.
 * @property authors A list of authors of the book.
 * @property publisher The (optional) publisher of the book.
 * @property categories An (optional) list of categories of the book.
 * @property publishedAt The (optional) year at which the book was published.
 * @property description An (optional) description of the book.
 * @property language The (optional) language of the book.
 * @property ratings An (optional) rating of the book.
 * @property images A map of images of the book.
 */
@Entity
class Book(
    val id: String,
    override val identifiers: Map<Identifier, String>,
    override val title: String,
    override val subtitle: String? = null,
    override val authors: List<String>,
    override val publisher: String? = null,
    override val categories: Set<String> = emptySet(),
    @JsonProperty("published")
    @JsonDeserialize(using = JacksonPublishDateDeserializer::class)
    @JsonSerialize(using = ToStringSerializer::class)
    override val publishedAt: TemporalAccessor? = null,
    override val description: String? = null,
    override val language: String? = null,
    override val ratings: Ratings? = null,
    override val images: Map<String, URL> = emptyMap()
) : AbstractBook()
