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
import nl.tudelft.booklab.catalogue.Title
import nl.tudelft.booklab.catalogue.TitleType
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
class ParseException : Exception()

/**
 * A parser that parses Dublin Core XML results into a list of [Book]s
 *
 * @author Christian Slothouber (f.c.slothouber@student.tudelft.nl)
 */
object DublinCoreParser {

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
     * parses a list of titles from a record
     *
     * @param record the record containing the title [Element]s
     * @return a list of [Title]s
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
     * all identifiers that are not a isbn id are discarded
     *
     * @param record the record [Element] the be parsed
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
                books.add(Book(parseTitles(record), parseAuthors(record), parseIds(record)))
            }
        } catch (e: Exception) {
            throw ParseException()
        }

        return books
    }
}
