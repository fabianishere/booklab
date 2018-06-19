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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * A Jackson deserializer for [User] classes.
 *
 * @property userService The service to lookup the user with.
 */
class JacksonUserDeserializer(private val userService: UserService) : StdDeserializer<User>(User::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): User? {
        val node: JsonNode = p.codec.readTree(p)
        val id = node["id"]?.let {
            if (it.canConvertToInt())
                it.asInt()
            else
                it.asText().toIntOrNull()
        }

        if (id != null) {
            return userService.findById(id)
        }

        val email = node["email"]?.textValue()

        if (email != null) {
            return userService.findByEmail(email)
        }

        return null
    }
}
