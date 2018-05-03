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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.URL

data class SruResult(
    @JacksonXmlProperty(localName = "records")
    val records: List<Record>
)

data class Record(
    @JacksonXmlProperty(localName = "recordData")
    val data: RecordData
)

data class RecordData(
    @JacksonXmlProperty(localName = "dc")
    val book: Book
)

data class Book(
    @JacksonXmlProperty(localName = "title")
    @JacksonXmlElementWrapper(useWrapping = false)
    val titles: List<Title>,

    @JacksonXmlProperty(localName = "creator")
    val author: String
)

data class Title(
    @JacksonXmlProperty(isAttribute = true, namespace = "xsi")
    val type: String,

    @JacksonXmlProperty(isAttribute = true, namespace = "xml", localName = "lang")
    val language: String) {
        @JacksonXmlText
        lateinit var value: String

        override fun toString(): String = "Title(value=$value, type=$type, language=$language)"
}

class XMLParser {

    private val mapper = XmlMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun parse(url: URL): SruResult = mapper.readValue(url)
}
