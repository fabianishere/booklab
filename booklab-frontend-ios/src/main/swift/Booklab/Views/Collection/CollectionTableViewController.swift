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

import UIKit
import Siesta
import CRRefresh
import Material

public class CollectionTableViewController: UITableViewController {
    public var userService: UserService!
    public var collectionService: CollectionService!
    
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

        // Configure edit button
        self.navigationItem.rightBarButtonItem = self.editButtonItem
    
        // Request the current user
        self.userService.me
            .addObserver(self)
            .addObserver(self.snackbarController!)
            .loadIfNeeded()
    
        
        // Configure refresh toggle
        self.tableView.cr.addHeadRefresh(animator: NormalHeaderAnimator()) { [weak self] in
            self?.collectionsResource?.load()
                .onCompletion { _ in self?.tableView.cr.endHeaderRefresh() }
        }
        
        // Load header view nib
        let nib = UINib(nibName: "Collection", bundle: Bundle.main)
        tableView.register(nib, forHeaderFooterViewReuseIdentifier: "header")
        tableView.sectionHeaderHeight = 55.0
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Update the collections if they were updated in another part of the
        // application.
        collectionsResource?.loadIfNeeded()
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
    
    @IBAction public func delete(sender: UIControl) {
        if sender is UIButton {
            let header = sender.superview?.superview?.superview as! CollectionHeaderView
            delete(at: header.section)
        } else {
            let header = sender.superview?.superview as! CollectionHeaderView
            delete(at: header.section)
        }
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
     * Delete a [BookCollection] at the given section.
     */
    public func delete(at section: Int) {
        let collection = collections[section]
        
        let alert = UIAlertController(
            title: "Delete collection",
            message: "Are you sure you want to delete this collection?",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Delete", style: .destructive) { _ in
            let _ = self.collectionService
                .delete(collection: collection)
                .useSnackbar(snackbarController: self.snackbarController!)
        })
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        self.present(alert, animated: true, completion: nil)
    }
    
    /**
     * Delete a [Book] from at the given indexPath.
     */
    public func deleteBook(at indexPath: IndexPath) {
        let collection = collections[indexPath.section]
        let book = collection.books[indexPath.row]
        
        let cell = tableView.cellForRow(at: indexPath)
        cell?.isUserInteractionEnabled = false
        
        let _ = self.collectionService
            .remove(from: collection, books: [book])
            .useSnackbar(snackbarController: self.snackbarController!)
            .onSuccess {
                self.tableView.beginUpdates()
                self.collections[indexPath.section] = $0.typedContent()!
                self.tableView.deleteRows(at: [indexPath], with: .top)
                self.tableView.endUpdates()
            }
            .onCompletion { _ in
                cell?.isUserInteractionEnabled = true
            }
    }
    
    /**
     * Move a [Book] to another index.
     */
    public func moveBook(from origin: IndexPath, to destination: IndexPath) {
        let originCollection = collections[origin.section]
        let destinationCollection = collections[destination.section]
        let book = originCollection.books[origin.row]
        
        if origin.section == destination.section {
            return
        }
       
        let _ = self.collectionService
            .remove(from: originCollection, books: [book])
            .flatMap { _ in
                return self.collectionService.add(to: destinationCollection, books: [book])
            }
            .useSnackbar(snackbarController: self.snackbarController!)
    }

    // MARK: - Table view data source
    public override func numberOfSections(in tableView: UITableView) -> Int {
        return collections.count
    }

    public override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return collections[section].books.count
    }

    public override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "book", for: indexPath) as! CatalogueBookTableViewCell
        let book = collections[indexPath.section].books[indexPath.row]
        cell.book = book
        return cell
    }
    
    public override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 100.0;
    }
    
    public override func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: "header") as! CollectionHeaderView
        let collection = collections[section]
        header.name.text = collection.name.uppercased()
        header.section = section
        return header
    }
    
    public override func tableView(_ tableView: UITableView, estimatedHeightForHeaderInSection section: Int) -> CGFloat {
        return 55.0
    }

    // Override to support conditional editing of the table view.
    public override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }

    // Override to support editing the table view.
    public override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            deleteBook(at: indexPath)
        }
    }

    // Override to support conditional rearranging of the table view.
    public override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    public override func tableView(_ tableView: UITableView, moveRowAt sourceIndexPath: IndexPath, to destinationIndexPath: IndexPath) {
        moveBook(from: sourceIndexPath, to: destinationIndexPath)
    }
    
    // MARK: Navigation
    public override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "detail",
           let destination = segue.destination as? CatalogueBookViewController,
           let index = tableView.indexPathForSelectedRow {
            destination.book = collections[index.section].books[index.row]
        }
    }
}

extension CollectionTableViewController : ResourceObserver {
    public func resourceChanged(_ resource: Resource, event: ResourceEvent) {
        if resource == self.userService.me, let user: User = resource.typedContent() {
            self.user = user
            self.collectionsResource = self.collectionService.get(for: user)
        } else {
            collections = resource.typedContent() ?? []
        }
    }
}
