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

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import io.ktor.auth.Principal
import nl.tudelft.booklab.backend.services.collection.BookCollection
import javax.persistence.Entity
import javax.validation.constraints.Email

/**
 * An entity representing a user account.
 *
 * @property id The identifier of the user.
 * @property email The email address of the user.
 * @property password A hashed password associated with the user.
 * @property collections The collection collections of the user.
 */
@Entity
data class User(
    val id: Int,
    @Email(message = "Please provide a valid email address for this user")
    val email: String,
    @JsonIgnore
    val password: String,
    @JsonBackReference
    val collections: Set<BookCollection> = emptySet()
) : Principal {

    override fun equals(other: Any?): Boolean {
        return other is User && id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String = "User(id=$id, email=$email)"
}
