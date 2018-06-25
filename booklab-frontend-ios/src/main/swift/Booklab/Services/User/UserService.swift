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

public class UserService {
    /**
     * The resource associated with this service.
     */
    public private(set) var resource: Resource
    
    /**
     * The current user session.
     */
    public var me: Resource {
        get { return resource.child("/me") }
    }
    
    /**
     * Construct a [UserService] instance.
     *
     * @param resource The resource associated with this service.
     */
    public init(resource: Resource) {
        self.resource = resource
    }
    
    /**
     * Register a user with the given username and password.
     *
     * @param email The email address of the user to register.
     * @param password The password of the user to register.
     * @param requestMutation A block to mutate the request.
     */
    public func register(email: String, password: String, requestMutation: @escaping Resource.RequestMutation = {_ in }) -> Request {
        let body = ["email" : email, "password" : password]
        return resource.request(.post, json: body, requestMutation: requestMutation)
    }
}
