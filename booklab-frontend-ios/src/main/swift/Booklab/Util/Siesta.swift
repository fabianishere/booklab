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

import Siesta

public extension Request {
    public func map(transform: @escaping (Entity<Any>) throws -> Entity<Any>) -> Request {
        return chained {
            guard case .success(let entity) = $0.response else {
                return .useThisResponse
            }
            
            do {
                return .useResponse(ResponseInfo(response: .success(try transform(entity)), isNew: $0.isNew))
            } catch let error {
                let requestError = RequestError(userMessage: "The transformation failed", cause: error, entity: entity)
                return .useResponse(ResponseInfo(response: .failure(requestError), isNew: $0.isNew))
            }
        }
    }
    
    public func flatMap(transform: @escaping (Entity<Any>) throws -> Request) -> Request {
        return chained {
            guard case .success(let entity) = $0.response else {
                return .useThisResponse
            }
            
            do {
                return .passTo(try transform(entity))
            } catch let error {
                let requestError = RequestError(userMessage: "The transformation failed", cause: error, entity: entity)
                return .useResponse(ResponseInfo(response: .failure(requestError), isNew: $0.isNew))
            }
        }
    }
}
