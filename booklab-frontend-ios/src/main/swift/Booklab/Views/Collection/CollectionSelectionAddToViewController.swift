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
 * A [CollectionSelectionDelegate] that adds the selection to the specified
 * collection.
 */
public class CollectionSelectionAddToViewController : CollectionSelectionViewController {
    var books: [Book]!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.delegate = self
    }
}

extension CollectionSelectionAddToViewController : CollectionSelectionDelegate {
    public func collectionSelectionViewController(_ selectionViewController: CollectionSelectionViewController, didSelect collection: BookCollection) {
        let _ = collectionService.add(to: collection, books: books)
            .useSnackbar(snackbarController: snackbarController!)
        self.motionDismissViewController()
    }
}
