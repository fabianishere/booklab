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
 * A [CollectionSelectionDelegate] that adds the selection to the specified
 * collection.
 */
public class RecommendationSelectionViewController : CollectionSelectionViewController {
    public var recommendationService: RecommendationService!
    public var candidates: [Book]!
    
    fileprivate var collection: BookCollection?
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.delegate = self
    }
    
    public override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "recommend",
            let collection = self.collection,
            let destination = segue.destination as? CatalogueTableViewController {
            print("DOING RECOMMENDATIONS", snackbarController!)
            recommendationService.recommend(collection: collection, candidates: candidates)
                    .useSnackbar(snackbarController: snackbarController!)
                    .onSuccess { entity in
                        destination.books = entity.typedContent() ?? []
                    }
        }
    }
}

extension RecommendationSelectionViewController : CollectionSelectionDelegate {
    public func collectionSelectionViewController(_ selectionViewController: CollectionSelectionViewController, didSelect collection: BookCollection) {
        self.collection = collection
        self.performSegue(withIdentifier: "recommend", sender: self)
    }
}
