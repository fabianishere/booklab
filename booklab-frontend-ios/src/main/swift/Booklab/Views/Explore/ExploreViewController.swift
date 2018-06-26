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
import Material
import URLNavigator

public class ExploreViewController : UIViewController {
    public var navigator: NavigatorType!
    public var catalogueService: CatalogueService!
    
    @IBOutlet var searchField: UITextField!
    var recommendedCollection: CatalogueCollectionViewController!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.prepareSearchField()
    }
    
    // Hide the keyboard if the user presses outside the text fields
    public override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.searchField.resignFirstResponder()
    }
    
    fileprivate func prepareSearchField() {
        searchField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 10, height: 20))
        searchField.leftViewMode = .always
        
        searchField.rightView = UIImageView(image: Icon.search)
        searchField.rightView?.tintColor = UIColor.lightGray
        searchField.rightView?.frame = CGRect(x: 10, y: 5, width: 40 , height: 25)
        searchField.rightView?.contentMode = .scaleAspectFit
        searchField.rightView?.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(self.search)))
        searchField.rightView?.isUserInteractionEnabled = true
        searchField.rightViewMode = .always
        
        searchField.layer.masksToBounds = false
        searchField.layer.shadowOpacity = 0.7
        searchField.layer.shadowOffset = CGSize(width: 3, height: 3)
        searchField.layer.shadowRadius = 15.0
        searchField.layer.shadowColor = UIColor.darkGray.cgColor
    }
    
    fileprivate func prepareRecommedations() {
        recommendedCollection.catalogueResource = catalogueService.find(query: "Harry Potter")
    }
    
    @objc func search() {
        if let controller = navigator.viewController(for: "/catalogue", context: searchField.text) {
            self.navigationController?.pushViewController(controller, animated: true)
        }
    }

    // MARK: - Navigation
    public override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "embed-recommended",
            let destination = segue.destination as? CatalogueCollectionViewController {
            recommendedCollection = destination
            prepareRecommedations()
        }
    }
}

extension ExploreViewController : TextFieldDelegate {
    public func textFieldDidEndEditing(_ textField: UITextField) {
        (textField as? ErrorTextField)?.isErrorRevealed = false
    }
    
    public func textFieldShouldClear(_ textField: UITextField) -> Bool {
        (textField as? ErrorTextField)?.isErrorRevealed = false
        return true
    }
    
    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        // Incase of search field, go to search
        if textField.tag == 0 {
            search()
            return true
        }
        
        if let nextField = textField.superview?.viewWithTag(textField.tag + 1) as? UITextField {
            nextField.becomeFirstResponder()
        } else {
            // Not found, so remove keyboard.
            textField.resignFirstResponder()
        }
        return true
    }
}
