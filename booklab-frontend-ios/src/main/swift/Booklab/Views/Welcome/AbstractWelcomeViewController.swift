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

public class AbstractWelcomeViewController : UIViewController {
    @IBOutlet var bottomContraint: NSLayoutConstraint!
    
    fileprivate var dismissButton: FlatButton!
    
    /**
     * Prefer a white status bat text since it matches the background better
     */
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
    
        self.prepareKeyboard()
    }
    
    // Hide the keyboard if the user presses outside the text fields
    public override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.view.endEditing(true)
    }
}

extension AbstractWelcomeViewController {
    internal func prepareTextField(textField: TextField) {
        textField.isPlaceholderUppercasedWhenEditing = true
        textField.placeholderAnimation = .default
        
        // Set the colors for the emailField, different from the defaults.
        textField.placeholderNormalColor = Color.white
        textField.placeholderActiveColor = Color.pink.base
        textField.detailColor = Color.pink.darken1
        textField.dividerNormalColor = Color.white
        textField.dividerActiveColor = Color.pink.base
        textField.detailColor = Color.pink.darken1
        textField.textColor = Color.white
        textField.leftViewNormalColor = Color.white
        textField.leftViewActiveColor = Color.pink.base
    }
    
    internal func prepareButton(button: Button) {
        button.titleColor = Color.white
        button.pulseColor = .white
        button.backgroundColor = Color.pink.base
    }
}

/**
 * We use this extension to prevent the keyboard from overlapping the login form elements.
 */
extension AbstractWelcomeViewController {
    fileprivate func prepareKeyboard() {
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: NSNotification.Name.UIKeyboardWillShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: NSNotification.Name.UIKeyboardWillHide, object: nil)
    }
    
    @objc fileprivate func keyboardWillShow(notification: NSNotification) {
        guard let userInfo = notification.userInfo,
            let keyboardSize = (userInfo[UIKeyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue else { return }
        bottomContraint.constant = keyboardSize.height + 10
        view.setNeedsLayout()
    }
    
    @objc fileprivate func keyboardWillHide(notification: NSNotification) {
        bottomContraint.constant = 50
        view.setNeedsLayout()
    }
}
