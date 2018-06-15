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

package nl.tudelft.booklab.backend.ktor

import io.ktor.routing.Routing

/**
 * An interface for configuring the routes of an application.
 */
interface Routes {
    /**
     * Configure the [Routing] feature of an application.
     */
    fun Routing.configure()

    companion object {
        /**
         * Construct a [Routes] object from the given lambda.
         *
         * @param block The block to configure the routing feature with.
         */
        fun from(block: Routing.() -> Unit): Routes = object : Routes {
            override fun Routing.configure() = block()
        }
    }
}
