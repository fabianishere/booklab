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

import { AuthConfig } from 'angular-oauth2-oidc';

/**
 * The OAuth 2 authorization configuration for the angular-oauth2-oidc package.
 */
export const authConfig: AuthConfig = {
    // The endpoint that issues the access tokens
    tokenEndpoint: 'http://localhost:8080/api/auth/token',

    // The SPA's id. The SPA is registered with this id at the auth-server
    clientId: 'test',

    // The client secret to be send to the server
    dummyClientSecret: 'test',

    // Set the scopes for the permissions the client should request
    scope: 'detection',

    // Flag to enable debugging information
    showDebugInformation: false,

    // Disable OIDC support as it is not supported by the server
    oidc: false
};
