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

package nl.tudelft.booklab.catalogue

import java.net.URL
import java.time.temporal.TemporalAccessor

/**
 * An abstract class representing a book implemented by catalogue providers.
 */
abstract class Book {
    /**
     * A map containing the identifiers of the book.
     */
    abstract val identifiers: Map<Identifier, String>

    /**
     * The title of the book.
     */
    abstract val title: String

    /**
     * The (optional) subtitle of the book.
     */
    abstract val subtitle: String?

    /**
     * A list of authors of the book.
     */
    abstract val authors: List<String>

    /**
     * The (optional) publisher of the book.
     */
    abstract val publisher: String?

    /**
     * An (optional) list of categories of the book.
     */
    abstract val categories: Set<String>

    /**
     * The (optional) date at which the book was published.
     */
    abstract val publishedAt: TemporalAccessor?

    /**
     * An (optional) description of the book.
     */
    abstract val description: String?

    /**
     * The (optional) language of the book.
     */
    abstract val language: String?

    /**
     * An (optional) rating of the book.
     */
    abstract val ratings: Ratings?

    /**
     * A map of images of the book.
     */
    abstract val images: Map<String, URL>

    /**
     * Determine whether the given object is equal to this [Book].
     *
     * @param other The object to check for equality.
     * @return `true` if both objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other is Book) {
            return identifiers.any { other.identifiers[it.key] == it.value }
        }

        return false
    }

    /**
     * Compute a hash code for this [Book] object.
     *
     * @return A hash code for the object.
     */
    override fun hashCode(): Int = identifiers.hashCode()

    /**
     * Return a string representation of this class.
     *
     * @return A string representation of this class.
     */
    override fun toString(): String = "Book(identifiers=$identifiers, title=$title, authors=$authors, ratings=$ratings)"
}

/**
 * Enumeration of identifier types for a book.
 */
enum class Identifier {
    /**
     * 10-digit version of International Standard Book Number as published as international standard ISO 2108.
     */
    ISBN_10,

    /**
     * 13-digit version of International Standard Book Number as published as international standard ISO 2108.
     */
    ISBN_13,

    /**
     * An internal identifier specific to the implementation used.
     */
    INTERNAL
}

/**
 * This contain represents the ratings of a [Book].
 *
 * @property average The average rating given by the user.
 * @property count The total amount of ratings given.
 */
data class Ratings(val average: Double, val count: Int) {
    /**
     * A private constructor so framework can construct this class dynamically.
     */
    private constructor() : this(0.0, 0)
}
