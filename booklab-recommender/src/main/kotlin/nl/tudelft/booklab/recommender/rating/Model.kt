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

package nl.tudelft.booklab.recommender.rating

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rating(
    @JsonProperty("isbn")
    val isbn10: String?,
    val isbn13: String?,
    @JsonProperty("average_rating")
    val rating: String
)

data class Results(
    @JsonProperty("books")
    val ratings: List<Rating>
) {
    fun contains(isbns: List<String>): Boolean {
        return ratings
            .map { it.isbn10 }
            .plus( ratings.map { it.isbn13 })
            .intersect(isbns)
            .isNotEmpty()
    }

    fun get(isbns: List<String>): Rating {
        if (contains(isbns)) { return ratings.filter { isbns.contains(it.isbn10) || isbns.contains(it.isbn13) }[0] }
        else throw NoSuchElementException()
    }
}
