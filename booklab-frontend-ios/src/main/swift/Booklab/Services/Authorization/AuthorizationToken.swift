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

import Foundation

/**
 * An authorization token provided by the [AuthorizationService].
 */
public struct AuthorizationToken : Codable, Equatable {
    /**
     * The active access token for the current session.
     */
    public let value: String
    
    /**
     * The date at which this token was issued.
     */
    public let issuedAt: Date
    
    /**
     * The amount of seconds after which the token expires.
     */
    public let expiresIn: TimeInterval
    
    /**
     * The scope the token applies to.
     */
    public let scope: String?
    
    /**
     * The state that was passed to the server.
     */
    public let state: String?
    
    /**
     * A flag to indicate whether the token has expired.
     */
    var isExpired: Bool {
        get { return Date() > issuedAt.addingTimeInterval(expiresIn) }
    }
    
    /**
     * Construct a [AuthorizationToken] from the given [Decoder] instance.
     *
     * @param decoder The [Decoder] to use.
     */
    public init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        
        self.value = try values.decode(String.self, forKey: CodingKeys.value)
        self.scope = try values.decodeIfPresent(String.self, forKey: CodingKeys.scope)
        self.state = try values.decodeIfPresent(String.self, forKey: CodingKeys.state)
        self.issuedAt = (try? values.decode(Date.self, forKey: CodingKeys.issuedAt)) ?? Date()
        self.expiresIn = try values.decode(TimeInterval.self, forKey: CodingKeys.expiresIn)
    }
    
    /**
     * Encode the [Authorization] with the given [Encoder].
     *
     * @param encoder The [Encoder] to use.
     */
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(self.value, forKey: CodingKeys.value)
        try container.encodeIfPresent(self.scope, forKey: CodingKeys.scope)
        try container.encodeIfPresent(self.state, forKey: CodingKeys.state)
        try container.encodeIfPresent(self.issuedAt, forKey: CodingKeys.issuedAt)
        try container.encodeIfPresent(self.expiresIn, forKey: CodingKeys.expiresIn)
    }
    
    /**
     * The keys we use for coding the class.
     */
    private enum CodingKeys: String, CodingKey {
        case value = "access_token"
        case issuedAt = "issued_at"
        case expiresIn = "expires_in"
        case scope = "scope"
        case state = "state"
    }
}

/**
 * This structure represents an OAuth2 authorization failure.
 */
public struct AuthorizationError : LocalizedError, Codable {
    /**
     * The type of error that occured.
     */
    public let type: String;
    
    /**
     * The error description.
     */
    public let description: String?;
    
    /**
     * The state that was given to the server.
     */
    public let state: String?
    
    public var errorDescription: String? {
        get { return description ?? type }
    }
    
    /**
     * The keys we use for coding the class.
     */
    private enum CodingKeys: String, CodingKey {
        case type = "error"
        case description = "error_description"
        case state
    }
}
