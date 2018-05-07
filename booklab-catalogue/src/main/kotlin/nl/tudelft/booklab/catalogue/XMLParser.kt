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

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import java.io.File

/**
 * A data class representing a book
 *
 * @property titles is a list of titles
 * @see Title
 * @property authors is a list of author names
 * @property ids is a list of isbn identifiers
 *
 * @author wollemat (f.c.slothouber@student.tudelft.nl)
 */
data class Book(
    val titles: List<Title>,
    val authors: List<String>,
    val ids: List<String>
)

/**
 * Enumeration representing different types of titles
 * as of yet only the main-title and sub-title types exist
 *
 * @author wollemat (f.c.slothouber@student.tudelft.nl)
 */
enum class TitleType {
    MAIN, SUB
}

/**
 * Data class that represents a title
 *
 * @property value the actual title
 * @property type the type of the title
 * @see TitleType
 *
 * @author wollemat (f.c.slothouber@student.tudelft.nl)
 */
data class Title(
    val value: String,
    val type: TitleType
)

/**
 * A exception thrown when for whatever reason the parser fails
 * @see Exception
 *
 * @author wollemat (f.c.slothouber@student.tudelft.nl)
 */
class ParseException : Exception()

/**
 * A parser that parses XML results into a list of books
 * @see Book
 *
 * @author wollemat (f.c.slothouber@student.tudelft.nl)
 */
class XMLParser {

    /**
     * Using a file with the XML source (can be a temporal file)
     * creates a document that can be parsed
     * @see Document
     * @see File
     *
     * @param file the file containing the XML
     *
     * @return the document
     */
    private fun createDocument(file: File): Document {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        doc.documentElement.normalize()
        return doc
    }

    /**
     * parses a list of titles from a record
     *
     * @param record the record containing the title elements
     * @see Element
     *
     * @return a list of titles
     * @see Title
     */
    private fun parseTitles(record: Element): List<Title> {
        val titleElements = record.getElementsByTagName("dc:title")
        val titles: MutableList<Title> = mutableListOf()

        for (i in 0 until titleElements.length) {
            val e = titleElements.item(i) as Element
            val type = if (e.getAttribute("xsi:type") == "dcx:maintitle") TitleType.MAIN else TitleType.SUB
            titles.add(Title(e.textContent, type))
        }

        return titles
    }

    /**
     * parses a list of isbn identifiers from a record
     * all identifiers that are not a isbn id are skipped
     *
     * @param record the record the be parsed
     * @see Element
     *
     * @return list of isbn numbers
     */
    private fun parseIds(record: Element): List<String> {
        val idElements = record.getElementsByTagName("dc:identifier")
        val ids: MutableList<String> = mutableListOf()

        for (i in 0 until idElements.length) {
            val id = idElements.item(i) as Element
            if (id.getAttribute("xsi:type") == "dcterms:ISBN") {
                ids.add(id.textContent)
            }
        }

        return ids
    }

    /**
     * parses a list of author names from a record
     *
     * @param record the record to be parsed
     * @see Element
     *
     * @return a list of names
     */
    private fun parseAuthors(record: Element): List<String> {
        val authorElements = record.getElementsByTagName("dc:creator")
        val authors: MutableList<String> = mutableListOf()

        for (i in 0 until authorElements.length) {
            authors.add(authorElements.item(i).textContent)
        }

        return authors
    }

    /**
     * parses XML to a list of books.
     * the source is located using the url given
     * @see Book
     *
     * @param url the url to the file to be parsed
     * @see URL
     *
     * @return the list of books
     */
    fun parse(url: URL): List<Book> {
        return parse(File(url.toURI()))
    }

    /**
     * parses XML to a list of books.
     * the source is directly passed
     * @see Book
     *
     * @param source the xml source
     *
     * @return the list of books
     */
    fun parse(source: String): List<Book> {
        val file = createTempFile()
        file.writeBytes(source.toByteArray())
        return parse(file)
    }

    /**
     * parses XML to a list of books.
     * the source is passed using a file
     * @see Book
     *
     * @param file the containing the xml source
     * @see File
     *
     * @return the list of books
     */
    fun parse(file: File): List<Book> {
        val books: MutableList<Book> = mutableListOf()

        try {
            val records = createDocument(file).documentElement.getElementsByTagName("srw:record")
            for (i in 0 until records.length) {
                val record = records.item(i) as Element
                books.add(Book(parseTitles(record), parseAuthors(record), parseIds(record)))
            }
        } catch (e: Exception) {
            throw ParseException()
        }

        return books
    }
}
