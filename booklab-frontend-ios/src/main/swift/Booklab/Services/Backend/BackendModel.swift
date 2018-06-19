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
import UIKit

/**
 * A response from the backend API interface.
 */
public enum BackendResponse<T : Codable> : Codable {
    /**
     * The type for the meta information of the response.
     */
    public typealias Meta = [String : Any]
    
    /**
     * The type for the links of the response.
     */
    public typealias Links = [String : URL]
    
    /**
     * The request was performed successfully.
     */
    case Success(T, Meta, Links)
    
    /**
     * The request failed.
     */
    case Failure(BackendError, Meta, Links)
    
    /**
     * Construct a [BackendResponse] from the given [Decoder] instance.
     *
     * @param decoder The [Decoder] to use.
     */
    public init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        let meta = try values.decodeIfPresent(Meta.self, forKey: CodingKeys.meta) ?? [:]
        let links = try values.decodeIfPresent(Links.self, forKey: CodingKeys.links) ?? [:]
        
        do {
            if values.contains(CodingKeys.data) {
                let data = try values.decode(T.self, forKey: CodingKeys.data)
                self = .Success(data, meta, links)
                return
            }
            
            let error = try values.decode(BackendError.self, forKey: CodingKeys.error)
            throw error
        } catch let error {
            self = .Failure(BackendError(
                code: "invalid_response",
                title: "Failed to convert the reponse into the appropriate object.",
                detail: error.localizedDescription
            ), meta, links)
        }
    }
    
    /**
     * Encode the [BackendResponse] with the given [Encoder].
     *
     * @param encoder The [Encoder] to use.
     */
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        switch self {
        case .Success(let data, let meta, let links):
            try container.encode(data, forKey: CodingKeys.data)
            try container.encodeIfPresent(meta, forKey: CodingKeys.meta)
            try container.encodeIfPresent(links, forKey: CodingKeys.links)
        case .Failure(let error, let meta, let links):
            try container.encode(error, forKey: CodingKeys.error)
            try container.encodeIfPresent(meta, forKey: CodingKeys.meta)
            try container.encodeIfPresent(links, forKey: CodingKeys.links)
        }
    }
    
    /**
     * The keys we use for coding the class.
     */
    private enum CodingKeys: String, CodingKey {
        case data
        case error
        case meta
        case links
    }
}

/**
 * An error response reeived from the server.
 */
public struct BackendError : Codable {
    /**
     * An application-specific error code, expressed as a string value.
     */
    let code: String
    
    /**
     * A short, human-readable summary of the problem that SHOULD NOT change
     * from occurrence to occurrence of the problem, except for purposes of
     * localization.
     */
    let title: String?
    
    /**
     * A human-readable explanation specific to this occurrence of the problem.
     * Like title, this field’s value can be localized.
     */
    let detail: String?
}

/**
 * An extension for the [BackendError] structure to make it conform to the [Error]
 * protocol.
 */
extension BackendError : Error {
    /**
     * The localized description of this error.
     */
    var localizedDescription: String {
        get {
            return title != nil && detail != nil ? "\(title!): \(detail!)" : code
        }
    }
}

/**
 * A health check response.
 */
public struct HealthCheck : Codable {
    /**
     * A flag to indicate the request was successful.
     */
    public let success: Bool
}

/**
 * An entity representing a book.
 */
public struct Book : Codable {
    /**
     * The identifiers of the book.
     */
    //public let identifiers: [BookIdentifier : String]
    
    /**
     * The title of the book.
     */
    public let title: String
    
    /**
     * The authors of the book.
     */
    public let authors: [String]
    
    /**
     * The subtitle of the book.
     */
    public let subtitle: String?
    
    /**
     * The publisher of the book.
     */
    public let publisher: String?
    
    /**
     * The date at which the book was published.
     */
    public let published: String?
    
    /**
     * The categories of the book.
     */
    public let categories: [String]
    
    /**
     * A description of the book.
     */
    public let description: String?
    
    /**
     * The language of the book.
     */
    public let language: String?
    
    /**
     * The ratings of the book.
     */
    public let ratings: BookRatings?
    
    /**
     * The links to the images of the book.
     */
    public let images: [String : URL]
}

/**
 * The ratings for a book.
 */
public struct BookRatings: Codable {
    /**
     * The average rating of a book.
     */
    public let average: Double
    
    /**
     * The total amount of ratings for a book.
     */
    public let count: Int
}

/**
 * A type of book identifier returned by the API.
 */
public enum BookIdentifier : String, Codable {
    case ISBN_10
    case ISBN_13
    case INTERNAL
}

/**
 * A book detection result from the server.
 */
public struct BookDetection : Codable {
    /**
     * The book matches of the detected book.
     */
    public let matches: [Book]
    
    /**
     * The rectangle in the image where the book was detected.
     */
    public let box: CGRect
}

/**
 * A book collection of a user.
 */
public struct BookCollection : Codable {
    /**
     * The identifier of the collection.
     */
    public let id: Int
    
    /**
     * The name of the collection.
     */
    public let name: String
    
    /**
     * The user of this collection.
     */
    public let user: User

    /**
     * The books in the collection.
     */
    public let books: [Book]
}

/**
 * A user on the server.
 */
public struct User : Codable {
    /**
     * The identifier of the user.
     */
    public let id: Int
    
    /**
     * The email of the user.
     */
    public let email: String
}
