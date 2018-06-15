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

package nl.tudelft.booklab.recommender

import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Identifier
import nl.tudelft.booklab.catalogue.Ratings
import java.net.URL

/**
 * A [Book] implementation for testing
 */
class TestBook(
    override val identifiers: Map<Identifier, String>,
    override val title: String,
    override val authors: List<String>,
    override val ratings: Ratings? = null
) : Book() {
    override val publisher: String? = null
    override val subtitle: String? = null
    override val categories = emptySet<String>()
    override val publishedAt = null
    override val description = null
    override val language = null
    override val images = emptyMap<String, URL>()
}
