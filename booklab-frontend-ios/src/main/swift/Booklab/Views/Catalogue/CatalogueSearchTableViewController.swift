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

public class CatalogueSearchTableViewController : CatalogueTableViewController {
    public var catalogueService: CatalogueService!
    
    var searchText: String?
    
    var searchResource: Resource? {
        didSet {
            oldValue?.removeObservers(ownedBy: self)
            
            searchResource?
                .addObserver(self)
                .addObserver(self.snackbarController!)
                .loadIfNeeded()
        }
    }

    private var searchController: UISearchController!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setup the Search Controller
        self.searchController = UISearchController(searchResultsController: nil)
        self.searchController.searchResultsUpdater = self
        self.searchController.obscuresBackgroundDuringPresentation = false
        self.searchController.isActive = true
        self.searchController.searchBar.placeholder = "Enter the name of a book or author"
        self.searchController.searchBar.text = searchText
        self.searchController.searchBar.delegate = self
        self.navigationItem.searchController = searchController
        self.navigationItem.hidesSearchBarWhenScrolling = false
        definesPresentationContext = true
    }
    
    public override func viewDidLayoutSubviews() {
        self.searchController.searchBar.sizeToFit()
    }
}

extension CatalogueSearchTableViewController : ResourceObserver {
    public func resourceChanged(_ resource: Resource, event: ResourceEvent) {
        books = resource.typedContent() ?? []
    }
}

extension CatalogueSearchTableViewController : UISearchResultsUpdating {
    public func updateSearchResults(for searchController: UISearchController) {
        if searchController.searchBar.text!.isEmpty {
            books = []
        } else {
            searchResource = catalogueService.find(query: searchController.searchBar.text!)
        }
    }
}

extension CatalogueSearchTableViewController : UISearchBarDelegate {
    public func searchBar(_ searchBar: UISearchBar, selectedScopeButtonIndexDidChange selectedScope: Int) {
        if searchBar.text!.isEmpty {
            books = []
        } else {
            searchResource = catalogueService.find(query: searchBar.text!)
        }
    }
}
