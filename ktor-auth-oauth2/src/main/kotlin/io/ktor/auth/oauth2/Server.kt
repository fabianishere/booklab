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

package io.ktor.auth.oauth2

import io.ktor.auth.Principal
import io.ktor.auth.oauth2.grant.GrantHandler
import io.ktor.auth.oauth2.repository.AccessTokenRepository
import io.ktor.auth.oauth2.repository.ClientRepository

/**
 * An OAuth 2.0 authorization server for the Ktor web framework.
 *
 * @property handlers A mapping from grant types to its respective handler.
 * @property clientRepository The repository responsible for managing clients.
 * @property tokenRepository The repository responsible for generating tokens.
 * @property defaultScopes A set of default scopes assigned when no scoped are requested by the client.
 */
class OAuthServer<C : Principal, U : Principal>(
    val handlers: Map<String, GrantHandler<C, U>>,
    val clientRepository: ClientRepository<C>,
    val tokenRepository: AccessTokenRepository<C, U>,
    val defaultScopes: Set<String> = emptySet()
)
