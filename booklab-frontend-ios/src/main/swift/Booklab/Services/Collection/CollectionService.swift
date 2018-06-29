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

public class CollectionService {
    /**
     * The resource associated with this service.
     */
    public private(set) var resource: Resource
    
    /**
     * Construct a [CollectionService] instance.
     *
     * @param resource The resource associated with this service.
     */
    public init(resource: Resource) {
        self.resource = resource
    }
    
    /**
     * Return the [Resource] for a [BookCollection].
     *
     * @param collection The collection to get the resource for.
     */
    public func resource(for collection: BookCollection) -> Resource {
        return resource.child(collection.id.description)
    }
    
    /**
     * Return the [BookCollection]s for a [User].
     *
     * @param user The user to get the collections for.
     */
    public func get(for user: User) -> Resource {
        return resource.withParam("user", user.id.description)
    }
    
    /**
     * Create a new [BookCollection] for the current user session.
     *
     * @param user The user to create the collection for
     * @param name The name of the collection.
     * @param books The books in the collection.
     */
    public func create(name: String, books: [Book] = []) -> Request {
        return resource
            .request(.post, json: ["name" : name, "books" : books.map { $0.id }])
            .onSuccess { entity in
                if let collection: BookCollection = entity.typedContent() {
                    let userResource = self.get(for: collection.user)
                    if let collections: [BookCollection] = userResource.typedContent() {
                        userResource.overrideLocalContent(with: collections + [collection])
                    }
                    userResource.invalidate()
                }
            }
    }
    
    /**
     * Delete the given [BookCollection] from the server.
     *
     * @param collection The collection to delete.
     */
    public func delete(collection: BookCollection) -> Request {
        let resource = self.resource(for: collection)
        return resource
            .request(.delete)
            .onSuccess { _ in
                let userResource = self.get(for: collection.user)
                if let collections: [BookCollection] = userResource.typedContent() {
                    userResource.overrideLocalContent(with: collections.filter { $0 != collection })
                }
                userResource.invalidate()
            }
    }
    
    /**
     * Send a mutation request for the books of a collection
     *
     * @param collection The collection to add the books to.
     * @param books The books to add to the collection.
     */
    fileprivate func mutate(collection: BookCollection,
                       books: [Book],
                       method: RequestMethod,
                       requestMutation: @escaping Resource.RequestMutation = {_ in }) -> Request {
        let resource = self.resource(for: collection)
        return resource
            .child("books")
            .request(method, json: ["books" : books.map { $0.id }], requestMutation: requestMutation)
            .onSuccess { entity in
                let collection: BookCollection = entity.typedContent()!
                
                // Update local resource
                resource.overrideLocalContent(with: collection)
                
                // Invalidate the user collections
                self.get(for: collection.user).invalidate()
            }
    }
    
    /**
     * Add a list of [Book]s to the given [BookCollection].
     *
     * @param collection The collection to add the books to.
     * @param books The books to add to the collection.
     */
    public func add(to collection: BookCollection, books: [Book]) -> Request {
        return mutate(collection: collection, books: books, method: .post)
    }
    
    /**
     * Remove a list of [Book]s to the given [BookCollection].
     *
     * @param collection The collection to add the books to.
     * @param books The books to add to the collection.
     */
    public func remove(from collection: BookCollection, books: [Book]) -> Request {
        return mutate(collection: collection, books: books, method: .delete)
    }
    
    /**
     * Remove a list of [Book]s to the given [BookCollection].
     *
     * @param collection The collection to add the books to.
     * @param books The books to add to the collection.
     */
    public func set(for collection: BookCollection, books: [Book]) -> Request {
        return mutate(collection: collection, books: books, method: .put)
    }
}
