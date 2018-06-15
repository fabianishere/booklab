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

package nl.tudelft.booklab.catalogue.sru

import nl.tudelft.booklab.catalogue.Book
import nl.tudelft.booklab.catalogue.Identifier
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A [Exception] thrown when for whatever reason the parser fails
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
class SruParseException : Exception()

/**
 * A parser that parses Dublin Core XML results into a list of [Book]s
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
object SruParser {
    /**
     * Using a [File] with the XML source (can be a temporary file)
     * creates a [Document] that can be parsed
     *
     * @param file the [File] containing the XML
     * @return the [Document]
     */
    private fun createDocument(stream: InputStream): Document {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        doc.documentElement.normalize()
        return doc
    }

    /**
     * Parse the main title from the book.
     *
     * @param record the record containing the title [Element]s
     * @return The main title of the book.
     */
    private fun parseTitle(record: Element): String {
        val titleElements = record.getElementsByTagName("dc:title")

        for (i in 0 until titleElements.length) {
            val e = titleElements.item(i) as Element
            if (e.getAttribute("xsi:type") == "dcx:maintitle")
                return e.textContent
        }

        return "<NO TITLE>"
    }

    /**
     * Parse the sub title from the book.
     *
     * @param record the record containing the title [Element]s
     * @return The sub title of the book.
     */
    private fun parseSubtitle(record: Element): String? {
        val titleElements = record.getElementsByTagName("dc:title")

        for (i in 0 until titleElements.length) {
            val e = titleElements.item(i) as Element
            if (e.getAttribute("xsi:type") == "dcx:subtitle")
                return e.textContent
        }

        return null
    }

    /**
     * parses a list of isbn identifiers from a record
     * all identifiers that are not a isbn id are discarded
     *
     * @param record the record [Element] the be parsed
     * @return list of isbn numbers
     */
    private fun parseIds(record: Element): Map<Identifier, String> {
        val idElements = record.getElementsByTagName("dc:identifier")
        val ids: MutableMap<Identifier, String> = mutableMapOf()

        for (i in 0 until idElements.length) {
            val id = idElements.item(i) as Element
            if (id.getAttribute("xsi:type") == "dcterms:ISBN") {
                val type = if (id.textContent.length == 13) Identifier.ISBN_13 else Identifier.ISBN_10
                ids[type] = id.textContent
            }
        }

        return ids
    }

    /**
     * parses a list of author names from a record
     *
     * @param record the record [Element] to be parsed
     * @return a list of author names
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
     * Parses a publisher from a record.
     *
     * @param record the record [Element] to be parsed
     * @return The name of the publisher or `null` if it could not be found.
     */
    private fun parsePublisher(record: Element): String? {
        val publisherElements = record.getElementsByTagName("dc:publisher")

        if (publisherElements.length > 0) {
            return publisherElements.item(0).textContent
        }

        return null
    }

    /**
     * parses Dublin Core XML to a list of [Book]s.
     * the source is passed using a [InputStream]
     *
     * @param stream the [InputStream] containing the XML source
     * @return the list of books
     */
    fun parse(stream: InputStream): List<Book> {
        val books: MutableList<Book> = mutableListOf()

        try {
            val records = createDocument(stream).documentElement.getElementsByTagName("srw:record")
            for (i in 0 until records.length) {
                val record = records.item(i) as Element
                books.add(
                    SruBook(
                        parseIds(record),
                        parseTitle(record),
                        parseSubtitle(record),
                        parseAuthors(record),
                        parsePublisher(record)
                    )
                )
            }
        } catch (e: Exception) {
            throw SruParseException()
        }

        return books
    }
}
