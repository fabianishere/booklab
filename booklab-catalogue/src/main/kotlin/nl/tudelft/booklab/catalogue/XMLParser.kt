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

data class Book(
    val titles: List<Title>,
    val author: String,
    val isbn: String
)

enum class TitleType {
    MAIN, SUB
}

data class Title(
    val value: String,
    val type: TitleType
)

class ParseException : Exception()

class XMLParser {

    private fun createDocument(url: URL): Document {
        val inputFile = File(url.toURI())
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(inputFile)
        doc.documentElement.normalize()
        return doc
    }

    private fun parseTitles(record: Element): List<Title> {
        val titleElements = record.getElementsByTagName("dc:title")
        val titles: MutableList<Title> = mutableListOf()

        for (j in 0 until titleElements.length) {
            val e = titleElements.item(j) as Element
            val type = if (e.getAttribute("xsi:type") == "dcx:maintitle") TitleType.MAIN else TitleType.SUB
            titles.add(Title(e.textContent, type))
        }

        return titles
    }

    private fun parseIsbn(record: Element): String {
        val ids = record.getElementsByTagName("dc:identifier")
        for (j in 0 until ids.length) {
            val id = ids.item(j) as Element
            if (id.getAttribute("xsi:type") == "dcterms:ISBN") {
                return id.textContent
            }
        }

        return "NO ISBN"
    }

    private fun parseAuthor(record: Element): String =
        record.getElementsByTagName("dc:creator").item(0).textContent

    fun parse(url: URL): List<Book> {
        val books: MutableList<Book> = mutableListOf()

        try {
            val records = createDocument(url).documentElement.getElementsByTagName("srw:record")
            for (i in 0 until records.length) {
                val record = records.item(i) as Element
                books.add(Book(parseTitles(record), parseAuthor(record), parseIsbn(record)))
            }
        } catch (e: Exception) {
            throw ParseException()
        }

        return books
    }
}
