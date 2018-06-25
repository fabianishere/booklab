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
import Pulley
import Siesta
import Material
import URLNavigator

public class DetectionDrawerViewController: UIViewController {
    public var navigator: NavigatorType!
    
    var detections: [DetectionBoxView] = [] {
        didSet {
            tableView.reloadData()
        }
    }
    
    @IBOutlet var tableView: UITableView!
    @IBOutlet var gripperView: UIView!
    @IBOutlet var topSeparatorView: UIView!
    @IBOutlet var bottomSeperatorView: UIView!
    @IBOutlet var gripperTopConstraint: NSLayoutConstraint!
    
    // We adjust our 'header' based on the bottom safe area using this constraint
    @IBOutlet var headerSectionHeightConstraint: NSLayoutConstraint!
    @IBOutlet var heightContraint: NSLayoutConstraint!
    @IBOutlet var topToolbar: UIToolbar!
    @IBOutlet var bottomToolbar: UIToolbar!
    @IBOutlet var selectButton: UIBarButtonItem!
    
    fileprivate var drawerBottomSafeArea: CGFloat = 0.0 {
        didSet {
            self.loadViewIfNeeded()
            
            // We'll configure our UI to respect the safe area. In our small demo app, we just want to adjust the contentInset for the tableview.
            tableView.contentInset = UIEdgeInsets(top: 0.0, left: 0.0, bottom: drawerBottomSafeArea, right: 0.0)
        }
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
        gripperView.layer.cornerRadius = 2.5
        
        // Fix for the toolbar failing on constraints
        topToolbar.autoresizingMask = [.flexibleWidth, .flexibleHeight, .flexibleBottomMargin]
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Show the bottom toolbar if we are in editing mode
        showBottomToolbar(tableView.isEditing)
        
        // You must wait until viewWillAppear -or- later in the view controller lifecycle in order to get a reference to Pulley via self.parent for customization.
        
        // UIFeedbackGenerator is only available iOS 10+. Since Pulley works back to iOS 9, the .feedbackGenerator property is "Any" and managed internally as a feedback generator.
        if #available(iOS 10.0, *) {
            let feedbackGenerator = UISelectionFeedbackGenerator()
            self.pulleyViewController?.feedbackGenerator = feedbackGenerator
        }
    }
    
    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Hide the bottom toolbar for other
        showBottomToolbar(false)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        // Update the toolbar frame
        bottomToolbar.frame = self.pulleyViewController!.tabsController!.tabBar.frame
    }
    
    @IBAction public func select(sender: UIBarButtonItem) {
        // We need to manually deselect all detection views
        if let indexPaths = tableView.indexPathsForSelectedRows {
            for indexPath in indexPaths {
                detections[indexPath.row].setSelected(false, animated: true)
            }
        }
        
        tableView.setEditing(!tableView.isEditing, animated: true)
        showBottomToolbar(tableView.isEditing)
        selectButton.title = tableView.isEditing ? "Done" : "Select"
        selectButton.style = tableView.isEditing ? .done : .plain
    }
    
    @IBAction public func cancel(sender: UIBarButtonItem) {
        pulleyViewController?.setDrawerPosition(position: .closed, animated: true)
        tableView.setEditing(false, animated: true)
        showBottomToolbar(false)
        
        // Reset primary content view controller
        if let primary = pulleyViewController?.primaryContentViewController as? DetectionViewController {
            primary.reset()
        }
    }
    
    @IBAction public func details(sender: FlatButton) {
        guard let cell = sender.superview?.superview as? UITableViewCell else {
            return
        }
        
        let index = tableView.indexPath(for: cell)
        let optionMenu = UIAlertController(title: nil, message: "Choose option", preferredStyle: .actionSheet)
        
        let detailAction = UIAlertAction(title: "View details", style: .default) { _ in
            self.performSegue(withIdentifier: "detail", sender: index)
        }
        
        let addAction = UIAlertAction(title: "Add to", style: .default) { _ in
            // TODO implement this action
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        
        optionMenu.addAction(detailAction)
        optionMenu.addAction(addAction)
        optionMenu.addAction(cancelAction)
        
        self.present(optionMenu, animated: true, completion: nil)
    }
    
    @IBAction public func selectBox(sender: DetectionBoxView) {
        if let index = detections.index(of: sender) {
            let indexPath = IndexPath(row: index, section: 0)
            
            // Deselect other cells first if we are not editing
            if !tableView.isEditing {
                if let indexPaths = tableView.indexPathsForSelectedRows {
                    for indexPath in indexPaths {
                        detections[indexPath.row].setSelected(false, animated: true)
                    }
                }
            }
            
            // Select the row associated with the box
            tableView.selectRow(at: indexPath, animated: true, scrollPosition: .middle)
            sender.setSelected(true, animated: true)
        }
    }
    
    public func showBottomToolbar(_ enabled: Bool) {
        let tabsController = pulleyViewController!.tabsController!
        if enabled {
            bottomToolbar.frame = tabsController.tabBar.frame
            tabsController.view.addSubview(bottomToolbar)
        } else {
            bottomToolbar.removeFromSuperview()
        }
    }
    
    public func deleteSelection() {
        self.tableView.beginUpdates()
        if let selection = self.tableView.indexPathsForSelectedRows {
            selection.forEach { self.detections.remove(at: $0.row) }
            self.tableView.deleteRows(at: selection, with: .top)
        }
        self.tableView.endUpdates()
    }
    
    // MARK: Navigation
    public override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "detail",
            let destination = segue.destination as? CatalogueBookViewController,
            let index = sender as? IndexPath ?? tableView.indexPathForSelectedRow {
            destination.book = detections[index.row].detection.matches[0]
        }
    }
    
    public override func shouldPerformSegue(withIdentifier identifier: String?, sender: Any?) -> Bool {
        if identifier == "detail" && tableView.isEditing {
            return false
        }
        return true
    }
}

extension DetectionDrawerViewController: PulleyDrawerViewControllerDelegate {
    public func collapsedDrawerHeight(bottomSafeArea: CGFloat) -> CGFloat {
        return 44.0 + bottomSafeArea
    }
    
    public func partialRevealDrawerHeight(bottomSafeArea: CGFloat) -> CGFloat {
        return 232.0 + bottomSafeArea
    }
    
    public func supportedDrawerPositions() -> [PulleyPosition] {
        return PulleyPosition.all
    }
    
    // This function is called by Pulley anytime the size, drawer position, etc. changes. It's best to customize your VC UI based on the bottomSafeArea here (if needed). Note: You might also find the `pulleySafeAreaInsets` property on Pulley useful to get Pulley's current safe area insets in a backwards compatible (with iOS < 11) way. If you need this information for use in your layout, you can also access it directly by using `drawerDistanceFromBottom` at any time.
    public func drawerPositionDidChange(drawer: PulleyViewController, bottomSafeArea: CGFloat) {
        // We want to know about the safe area to customize our UI. Our UI customization logic is in the didSet for this variable.
        drawerBottomSafeArea = bottomSafeArea
        
        // Set height of content view
        // We add some compensation since it won't cover the whole view otherwise
        heightContraint.constant = drawer.drawerDistanceFromBottom.distance + bottomSafeArea + 29
        
        /*
         Some explanation for what is happening here:
         1. Our drawer UI needs some customization to look 'correct' on devices like the iPhone X, with a bottom safe area inset.
         2. We only need this when it's in the 'collapsed' position, so we'll add some safe area when it's collapsed and remove it when it's not.
         3. These changes are captured in an animation block (when necessary) by Pulley, so these changes will be animated along-side the drawer automatically.
         */
        if drawer.drawerPosition == .collapsed {
            headerSectionHeightConstraint.constant = 44.0 + drawerBottomSafeArea
        } else {
            headerSectionHeightConstraint.constant = 44.0
        }
        
        // Handle tableview scrolling / searchbar editing
        tableView.isScrollEnabled = drawer.drawerPosition == .open || drawer.drawerPosition ==
            .partiallyRevealed || drawer.currentDisplayMode == .leftSide

        if drawer.currentDisplayMode == .leftSide {
            topSeparatorView.isHidden = drawer.drawerPosition == .collapsed
            bottomSeperatorView.isHidden = drawer.drawerPosition == .collapsed
        } else {
            topSeparatorView.isHidden = false
            bottomSeperatorView.isHidden = true
        }
    }
    
    /// This function is called when the current drawer display mode changes. Make UI customizations here.
    public func drawerDisplayModeDidChange(drawer: PulleyViewController) {
        gripperTopConstraint.isActive = drawer.currentDisplayMode == .bottomDrawer
    }
}

extension DetectionDrawerViewController : UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return detections.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "detection", for: indexPath) as! CatalogueBookTableViewCell
        let view = detections[indexPath.row]
        let book = view.detection.matches[0]
        cell.book = book
        return cell
    }
    
    // Override to support conditional editing of the table view.
    public func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    
    // Override to support editing the table view.
    public func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.beginUpdates()
            detections.remove(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: .fade)
            tableView.endUpdates()
        }
    }
}

extension DetectionDrawerViewController : UITableViewDelegate {
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 90.0
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let view = detections[indexPath.row]
        view.setSelected(true, animated: true)
    }
    
    public func tableView(_ tableView: UITableView, didDeselectRowAt indexPath: IndexPath) {
        let view = detections[indexPath.row]
        view.setSelected(false, animated: true)
    }
}
