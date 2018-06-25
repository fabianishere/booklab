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
import UIKit

public class DetectionService {
    /**
     * The resource associated with this service.
     */
    public private(set) var resource: Resource
    
    /**
     * Construct a [DetectionService] instance.
     *
     * @param resource The resource associated with this service.
     */
    public init(resource: Resource) {
        self.resource = resource
    }

    /**
     * Detect the books in the given [UIImage].
     *
     * @param image The image to detect the books in.
     * @return The [Request] sent to the server returning a list of [BookDetection]s.
     */
    public func detect(in image: UIImage) -> Request {
        return resource.request(.post,
                                data: UIImageJPEGRepresentation(image, 1.0)!,
                                contentType: "image/png")
    }
}
