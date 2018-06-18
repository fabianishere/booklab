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
import RxSwift

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
     * The [DisposeBag] to use for the [Observable] subscriptions.
     */
    private let disposeBag = DisposeBag()
    
    /**
     * Construct a [BackendService] instance.
     *
     * @param authorization The [AuthorizationService] for the access tokens.
     * @param baseUrl The base url to use for the API calls.
     */
    public init(authorization: AuthorizationService, baseUrl: URLConvertible) {
        self.authorization = authorization
        self.service = Service(baseURL: baseUrl, standardTransformers: [.text, .image])
        
        // Configure the authorization header
        service.configure("**") {
            if let token: AuthorizationToken = authorization.token.latestData?.typedContent() {
                $0.headers["Authorization"] = "Bearer \(token.value)"
            }
        }
        
        // The endpoint transformers
        let decoder = JSONDecoder()
        
        // Health
        health = service.resource("/health")
        service.configureTransformer("/health") {
            try decoder.decode(BackendResponse<HealthCheck>.self, from: $0.content)
        }
        
        
        // Catalogue Endpoint
        catalogue = service.resource("/catalogue")
        service.configureTransformer("/catalogue") {
            try decoder.decode(BackendResponse<[Book]>.self, from: $0.content)
        }
        
        service.configureTransformer("/catalogue/*") {
            try decoder.decode(BackendResponse<Book>.self, from: $0.content)
        }
        
        // Detection endpoint
        detection = service.resource("/detection")
        service.configureTransformer("/detection") {
            try decoder.decode(BackendResponse<BookDetection>.self, from: $0.content)
        }
        
        // Collection endpoint
        collections = service.resource("/collections")
        service.configureTransformer("/collections") {
            try decoder.decode(BackendResponse<[BookCollection]>.self, from: $0.content)
        }
        
        service.configureTransformer("/collections/*") {
            try decoder.decode(BackendResponse<BookCollection>.self, from: $0.content)
        }
        
        // User endpoint
        users = service.resource("/users")
        service.configureTransformer("/users") {
            try decoder.decode(BackendResponse<[User]>.self, from: $0.content)
        }
        
        service.configureTransformer("/users/*") {
            try decoder.decode(BackendResponse<User>.self, from: $0.content)
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
}
