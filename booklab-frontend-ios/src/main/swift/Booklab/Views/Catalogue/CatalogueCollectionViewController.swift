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
import Siesta

class CatalogueCollectionViewController : UICollectionViewController {
    var catalogueResource: Resource? {
        didSet {
            oldValue?.removeObservers(ownedBy: self)
            
            catalogueResource?
                .addObserver(self)
                .addObserver(self.snackbarController!)
                .loadIfNeeded()
        }
    }
    
    var catalogueResults: [Book] = [] {
        didSet {
            collectionView?.reloadData()
        }
    }
    
    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "detail",
            let destination = segue.destination as? CatalogueBookViewController,
            let cell = sender as? CatalogueCollectionViewCell,
            let index = collectionView?.indexPath(for: cell) {
            destination.book = catalogueResults[index.row]
        }
    }

    // MARK: UICollectionViewDataSource

    override func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }


    override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return catalogueResults.count
    }

    override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "book", for: indexPath) as! CatalogueCollectionViewCell
        cell.book = catalogueResults[indexPath.row]
        return cell
    }
}

extension CatalogueCollectionViewController : ResourceObserver {
    public func resourceChanged(_ resource: Resource, event: ResourceEvent) {
        catalogueResults = resource.typedContent() ?? []
    }
}
