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
import Siesta
import SiestaUI

/**
 * A service that communicates with the backend over a RESTful API interface.
 */
public class BackendService {
    /**
     * The Siesta [Service] to use to connect to the backend.
     */
    private let service: Service

    /**
     * The [AuthorizationService] to use for getting the access token.
     */
    private let authorization: AuthorizationService
    
    /**
     * Construct a [BackendService] instance.
     *
     * @param authorization The [AuthorizationService] for the access tokens.
     * @param baseUrl The base url to use for the API calls.
     */
    public init(authorization: AuthorizationService, baseUrl: URLConvertible) {
        self.authorization = authorization
        self.service = Service(baseURL: baseUrl, standardTransformers: [.text, .image])
        
        service.configure("**") {
            $0.useNetworkActivityIndicator()
            
            // Configure the authorization header
            if let token: AuthorizationToken = authorization.token.latestData?.typedContent() {
                $0.headers["Authorization"] = "Bearer \(token.value)"
            }
        }
        
        // Health
        health = service.resource("/health")
        service.configure("/health") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<HealthCheck>(), contentTypes: ["*/json"])
        }
        
        // Catalogue Endpoint
        catalogue = service.resource("/catalogue")
        service.configure("/catalogue") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<[Book]>(), contentTypes: ["*/json"])
        }
        service.configure("/catalogue/*") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<Book>(), contentTypes: ["*/json"])
        }
        
        // Detection endpoint
        detection = service.resource("/detection")
        service.configure("/detection") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<[BookDetection]>(), contentTypes: ["*/json"])
        }
        
        // Detection endpoint
        recommendations = service.resource("/recommendations")
        service.configure("/recommendations") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<[Book]>(), contentTypes: ["*/json"])
        }
        
        // Collection endpoint
        collections = service.resource("/collections")
        service.configure("/collections", requestMethods: [.get]) {
            $0.pipeline[.parsing].add(BackendMessageExtractor<[BookCollection]>(), contentTypes: ["*/json"])
        }
        service.configure("/collections", requestMethods: [.post]) {
            $0.pipeline[.parsing].add(BackendMessageExtractor<BookCollection>(), contentTypes: ["*/json"])
        }
        service.configure("/collections/*", requestMethods: [.get, .post, .put]) {
            $0.pipeline[.parsing].add(BackendMessageExtractor<BookCollection>(), contentTypes: ["*/json"])
        }
        service.configure("/collections/*/books") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<BookCollection>(), contentTypes: ["*/json"])
        }
        
        // User endpoint
        users = service.resource("/users")
        service.configure("/users", requestMethods: [.get]) {
            $0.pipeline[.parsing].add(BackendMessageExtractor<[User]>(), contentTypes: ["*/json"])
        }
        service.configure("/users", requestMethods: [.post]) {
            $0.pipeline[.parsing].add(BackendMessageExtractor<User>(), contentTypes: ["*/json"])
        }
        service.configure("/users/*") {
            $0.pipeline[.parsing].add(BackendMessageExtractor<User>(), contentTypes: ["*/json"])
        }
        
        // Invalidate the resources when a token is requested
        authorization.token.addObserver(owner: self) { _, event in
            if case .requested = event {
                self.service.invalidateConfiguration()
                self.service.wipeResources()
            }
        }
    }   
    
    /**
     * The health check resource
     */
    public let health: Resource
    
    /**
     * The catalogue resource.
     */
    public let catalogue: Resource
    
    /**
     * The detection endpoint.
     */
    public let detection: Resource
    
    /**
     * The collection endpoint.
     */
    public let collections: Resource
    
    /**
     * The user endpoint.
     */
    public let users: Resource
    
    /**
     * The recommendation endpoint.
     */
    public let recommendations: Resource
}

/**
 * A [ResponseTransformer] that maps a response to the response data
 */
public struct BackendMessageExtractor<T : Codable>: ResponseTransformer {
    /**
     * The [JSONDecoder] we use to decode the responses.
     */
    let decoder = JSONDecoder()
    
    public func process(_ response: Response) -> Response {
        switch response {
        case .success(let entity):
            do {
                let data = entity.content as! Data
                switch try decoder.decode(BackendResponse<T>.self, from: data) {
                case .Success(let value, _, _):
                    return .success(Entity(content: value, contentType: entity.contentType))
                case .Failure(let cause, _, _):
                    throw cause
                }
            } catch let error {
                return .failure(RequestError(userMessage: "Failed to parse response", cause: error, entity: entity))
            }
    
        case .failure(var error):
            guard let data: Data = error.entity?.typedContent() else {
                return response
            }
            do {
                switch try decoder.decode(BackendResponse<T>.self, from: data) {
                case .Success(let value, _, _):
                    // This should not happen since successful responses should be
                    // marked as failures
                    return .success(Entity(content: value, contentType: error.entity?.contentType ?? ""))
                case .Failure(let cause, _, _):
                    error.cause = cause
                }
            } catch let cause {
                error.cause = cause
            }
            return .failure(error)
        }
    }
}
