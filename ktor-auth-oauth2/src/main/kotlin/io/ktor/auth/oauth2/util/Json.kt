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

package io.ktor.auth.oauth2.util

import io.ktor.auth.Principal
import io.ktor.auth.oauth2.OAuthError
import io.ktor.auth.oauth2.grant.Grant
import org.json.simple.JSONObject

/**
 * Serialize the given [OAuthError] into a JSON text format.
 *
 * @param state The optional state property to include.
 * @return The error serialized in JSON format.
 */
fun OAuthError.toJson(state: String? = null): String {
    val properties = mutableMapOf(
        "error" to type
    )
    state?.let { properties["state"] = it }
    message?.let { properties["error_description"] = it }
    return JSONObject.toJSONString(properties)
}

/**
 * Serialize the given [Grant] into a JSON text format.
 *
 * @return The grant serialized in JSON format.
 */
fun <C : Principal, U : Principal> Grant<C, U>.toJson(): String {
    val properties = mutableMapOf(
        "grant_type" to accessToken.type,
        "access_token" to accessToken.token,
        "created_at" to accessToken.issuedAt.toEpochMilli()
    )
    refreshToken?.let { properties.put("refresh_token", it) }
    accessToken.expiresIn?.let { properties.put("expires_in", it.seconds) }
    state?.let { properties.put("state", it) }
    return JSONObject.toJSONString(properties)
}
