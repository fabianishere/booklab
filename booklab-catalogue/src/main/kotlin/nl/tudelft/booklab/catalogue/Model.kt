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

/**
 * A data class representing a book
 *
 * @property titles is a list of all [Title]s and subtitles
 * @property authors is a list of author names
 * @property ids is a list of isbn identifiers
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
data class Book(
    val titles: List<Title>,
    val authors: List<String>,
    val ids: List<String>,
    val nur: Int
)

enum class Nur {
    UNKNOWN, KNOWN
}

/**
 * Enumeration representing difference between main title and
 * a subtitle
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
enum class TitleType {
    MAIN, SUB
}

/**
 * A data class that represents a book title
 *
 * @property value the actual title
 * @property type the [TitleType]
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
data class Title(
    val value: String,
    val type: TitleType
)
