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

public class RecommendationService {
    /**
     * The resource associated with this service.
     */
    public private(set) var resource: Resource
    
    /**
     * Construct a [RecommendationService] instance.
     *
     * @param resource The resource associated with this service.
     */
    public init(resource: Resource) {
        self.resource = resource
    }
 
    /**
     * Find recommendations for the given candiate books and the collection to
     * base the recommendation on.
     *
     * @param query The query to use for finding the books.
     * @param max The number of results to return.
     * @return The [Request] for a list of [Book]s.
     */
    public func recommend(collection: BookCollection, candidates: [Book]) -> Request {
        return resource
            .withParam("collection", collection.id.description)
            .request(.post, json: ["candidates" : candidates.map { $0.id }])
    }
}
