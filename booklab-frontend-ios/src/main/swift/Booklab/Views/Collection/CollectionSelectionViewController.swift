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

public class CollectionSelectionViewController : UITableViewController {
    public var userService: UserService!
    public var collectionService: CollectionService!
    
    public var delegate: CollectionSelectionDelegate?
    
    var collectionsResource: Resource? {
        didSet {
            oldValue?.removeObservers(ownedBy: self)
            
            collectionsResource?
                .addObserver(self)
                .addObserver(self.snackbarController!)
                .loadIfNeeded()
        }
    }
    
    var collections: [BookCollection] = [] {
        didSet {
            tableView.reloadData()
        }
    }
    
    var user: User!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Request the current user
        self.userService.me
            .addObserver(self)
            .addObserver(self.snackbarController!)
            .loadIfNeeded()
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.collectionsResource?.loadIfNeeded()
    }
    
    public override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    public override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return collections.count + 1
    }
    
    public override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == collections.count {
            return tableView.dequeueReusableCell(withIdentifier: "add", for: indexPath)
        } else {
            let cell = tableView.dequeueReusableCell(withIdentifier: "collection", for: indexPath)
            let collection = collections[indexPath.row]
            cell.textLabel?.text = collection.name
            cell.detailTextLabel?.text = "\(collection.books.count) books"
            return cell
        }
    }
    
    public override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row == collections.count {
            self.tableView.deselectRow(at: indexPath, animated: true)
        } else {
            let collection = collections[indexPath.row]
            delegate?.collectionSelectionViewController(self, didSelect: collection)
        }
    }
    
    // Override to support editing the table view.
    public override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            delete(at: indexPath)
        }
    }
    
    @IBAction public func cancel() {
        delegate?.collectionSelectionViewControllerDidCancel(self)
        self.motionDismissViewController()
    }
    
    // Handler for creating a collection
    @IBAction public func create() {
        let alertController = UIAlertController(title: "Create collection",
                                                message: "Enter name for collection",
                                                preferredStyle: .alert)
        
        let confirmAction = UIAlertAction(title: "Create", style: .default) { (_) in
            let name = alertController.textFields?[0].text
            self.create(name: name!)
        }
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { (_) in }
        
        alertController.addTextField { (textField) in
            textField.placeholder = "Enter Name"
        }
        
        alertController.addAction(confirmAction)
        alertController.addAction(cancelAction)
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    /**
     * Create a [BookCollection] with the given name.
     */
    public func create(name: String) {
        let _ = self.collectionService
            .create(name: name)
            .useSnackbar(snackbarController: self.snackbarController!)
    }
    
    /**
     * Delete the collection at the given index path.
     */
    public func delete(at indexPath: IndexPath) {
        let collection = collections[indexPath.row]
        let alert = UIAlertController(
            title: "Delete collection",
            message: "Are you sure you want to delete this collection?",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Delete", style: .destructive) { _ in
            self.tableView.beginUpdates()
            self.collectionService
                .delete(collection: collection)
                .useSnackbar(snackbarController: self.snackbarController!)
                .onSuccess { _ in
                    self.tableView.deleteRows(at: [indexPath], with: .fade)
                }
                .onCompletion { _ in
                    self.tableView.endUpdates()
                }
        })
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        self.present(alert, animated: true, completion: nil)
    }
}

extension CollectionSelectionViewController : ResourceObserver {
    public func resourceChanged(_ resource: Resource, event: ResourceEvent) {
        if resource == self.userService.me, let user: User = resource.typedContent() {
            self.user = user
            self.collectionsResource = self.collectionService.get(for: user)
        } else {
            collections = resource.typedContent() ?? []
        }
    }
}
