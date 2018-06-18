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
 * A service for managing the authorization of the client.
 */
public class AuthorizationService {
    /**
     * The Siesta [Service] to do the requests.
     */
    private let service: Service
    
    /**
     * The id of the client.
     */
    public let clientId: String
    
    /**
     * The secret of the client.
     */
    public let clientSecret: String
    
    /**
     * The token [Resource].
     */
    public let token: Resource
    
    /**
     * Construct an [AuthorizationService] instance.
     *
     * @param baseUrl The base url of the authorization API.
     * @param clientId The identifier of the client.
     * @param clientSecret The client secret.
     */
    public init(baseUrl: URLConvertible, clientId: String, clientSecret: String) {
        self.clientId = clientId
        self.clientSecret = clientSecret
        self.service = Service(baseURL: baseUrl, standardTransformers: [.text, .image])
        self.token = service.resource("/token")
        
        service.configure("/token") {
            $0.pipeline[.parsing].add(OAuth2MessageExtractor(), contentTypes: ["*/json"])
        }
        
        self.configureInvalidationTimer()
    }
    
    /**
     * Configure the invalidation timer of the token resource.
     */
    private func configureInvalidationTimer() {
        // We add a timer for invalidating the token resource if the token has
        // expired.
        var timer: Timer? = nil
        self.token.addObserver(owner: self) { entity, event in
            print(event)
            switch event {
            case .newData:
                let token: AuthorizationToken? = entity.typedContent()
                timer?.invalidate()
                timer = Timer(fire: token!.expiresAt, interval: 0, repeats: false) { timer in
                    timer.invalidate()
                    self.token.invalidate()
                }
                
                // Schedule the timer on the main run loop
                RunLoop.main.add(timer!, forMode: .commonModes)
            case .error, .requestCancelled:
                timer?.invalidate()
            default:
                return
            }
        }
    }
    
    /**
     * Authorize with the user password credential.
     *
     * @param username The username to authorize with.
     * @param password The password to authorize with.
     * @param scope The scope to request a token for.
     */
    public func authorize(username: String, password: String, scope: String? = nil) -> Request {
        let properties = [
            "grant_type" : "password",
            "client_id" : clientId,
            "client_secret" : clientSecret,
            "username" : username,
            "password" : password,
            "scope" : scope ?? ""
        ]
        return self.token.request(.post, urlEncoded: properties)
    }
    
    /**
     * Authorize with the client credentials.
     *
     * @param username The username to authorize with.
     * @param password The password to authorize with.
     * @param scope The scope to request a token for.
     */
    public func authorize(scope: String? = nil) -> Request {
        let properties = [
            "grant_type" : "client_credentials",
            "client_id" : clientId,
            "client_secret" : clientSecret,
            "scope" : scope ?? ""
        ]
        return self.token.request(.post, urlEncoded: properties)
    }
}

/**
 * A [ResponseTransformer] that maps a response to an OAuth2 response.
 */
struct OAuth2MessageExtractor: ResponseTransformer {
    /**
     * The [JSONDecoder] we use to decode the responses.
     */
    let decoder = JSONDecoder()
    
    func process(_ response: Response) -> Response {
        switch response {
        case .success(let entity):
            do {
                if let data = entity.content as? Data {
                    let token = try decoder.decode(AuthorizationToken.self, from: data)
                    return .success(Entity(content: token, contentType: entity.contentType))
                } else {
                    return response
                }
            } catch let error {
                return .failure(RequestError(userMessage: "Failed to parse response", cause: error, entity: entity))
            }
            
        case .failure(var error):
            do {
                if let data = error.entity?.content as? Data {
                    error.cause = try decoder.decode(AuthorizationError.self, from: data)
                }
            } catch let cause {
                error.cause = cause
            }
            return .failure(error)
        }
    }
}
