/*
 * Copyright 2018 Fabian Mastenbroek.
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

import io.ktor.auth.oauth2.OAuthError
import io.ktor.auth.oauth2.grant.Authorization
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import java.net.URI

/**
 * Convert the given [OAuthError] to a redirect uri.
 *
 * @param base The base uri to use.
 * @return The uri to redirect to.
 */
fun OAuthError.toRedirectUri(base: URI, state: String? = null): String = URLBuilder().run {
    takeFrom(base)
    parameters.apply {
        append("error", type)
        message?.let { append("error_description", it) }
        state?.let { append("state", it) }
    }
    buildString()
}

/**
 * Convert an authorization response to a redirect uri.
 *
 * @param base The base uri to use.
 * @return The uri to redirect to.
 */
fun Authorization.toRedirectUri(base: URI): String = URLBuilder().run {
    takeFrom(base)
    parameters.appendAll(this@toRedirectUri.parameters)
    buildString()
}
