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

import nl.tudelft.booklab.backend.services.catalogue.Book
import nl.tudelft.booklab.backend.services.user.User
import javax.persistence.Entity
import javax.validation.constraints.NotBlank

/**
 * An entity representing a collection collection.
 *
 * @property id The identifier of the collection.
 * @property user The owner of this collection collection.
 * @property name The name of the collection.
 * @property books The list of books in this collection.
 */
@Entity
data class BookCollection(
    val id: Int,
    var user: User?,
    @NotBlank(message = "The name of the collection should not be blank")
    val name: String,
    val books: Set<Book>
) {
    override fun toString(): String = "BookCollection(id=$id, name=$name, books=$books)"
}
