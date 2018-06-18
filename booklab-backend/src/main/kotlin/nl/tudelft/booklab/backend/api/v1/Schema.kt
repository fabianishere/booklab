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

package nl.tudelft.booklab.backend.api.v1

import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.pipeline.PipelineContext
import io.ktor.request.uri
import nl.tudelft.booklab.backend.baseUrl

/**
 * A base class for responses returned by the REST interface.
 */
sealed class ApiResponse(
    open val links: Map<String, Any>,
    open val meta: Map<String, Any>
) {
    /**
     * A response to indicate a success.
     *
     * @property data The data associated with the response.
     * @property links The links in the response.
     * @property meta The meta information of the response.
     */
    data class Success<T>(
        val data: T,
        override val links: Map<String, String> = emptyMap(),
        override val meta: Map<String, Any> = emptyMap()
    ) : ApiResponse(links, meta)

    /**
     * A response to indicate failure.
     *
     * @property error The error that occurred.
     * @property links The references in this error.
     * @property meta A meta object containing non-standard meta-information about the error.
     */
    data class Failure(
        val error: ApiError,
        override val links: Map<String, String> = emptyMap(),
        override val meta: Map<String, Any> = emptyMap()
    ) : ApiResponse(links, meta)
}

/**
 * This class represents the shape of an error returned by the application.
 *
 * @property code An application-specific error code, expressed as a string value.
 * @property title A short, human-readable summary of the problem that SHOULD NOT change from occurrence to
 * occurrence of the problem, except for purposes of localization.
 * @property detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 */
data class ApiError(
    val code: String,
    val title: String? = null,
    val detail: String? = null
)

/**
 * Respond with success to the given request.
 *
 * @param data The data associated with the response.
 * @param links The links in the response.
 * @param meta The meta information of the response.
 */
fun <T> PipelineContext<Unit, ApplicationCall>.Success(
    data: T,
    links: Map<String, String> = emptyMap(),
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Success<T> {
    val request = context.request
    return ApiResponse.Success(data, links + Pair("self", application.baseUrl + request.uri), meta)
}

/**
 * Indicate to the client that the request was malformed.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun InvalidRequest(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "invalid_request", title = "The request that was received was invalid", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}

/**
 * Indicate the the client that the requested method was not allowed.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun MethodNotAllowed(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "method_not_allowed", title = "The requested method is not allowed on the specified resource.", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}

/**
 * Indicate to the client that an internal server error occurred.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun ServerError(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "server_error", title = "An internal server error occurred while processing the request", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}

/**
 * Indicate to the client that the resource already exists.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun ResourceAlreadyExists(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "resource_exists", title = "The resource already exists on the server", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}

/**
 * Indicate to the client that it accesses a resource it is not allowed to.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun Forbidden(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "forbidden", title = "You are not allowed access to the specified resource", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}

/**
 * Indicate to the client that it accesses a non-existent resource.
 *
 * @param detail A human-readable explanation specific to this occurrence of the problem. Like title, this
 * field’s value can be localized.
 * @param meta A meta object containing non-standard meta-information about the error.
 */
fun NotFound(
    detail: String? = null,
    meta: Map<String, Any> = emptyMap()
): ApiResponse.Failure {
    val error = ApiError(code = "not_found", title = "The specified resource does not exist", detail = detail)
    return ApiResponse.Failure(
        error = error,
        meta = meta
    )
}
