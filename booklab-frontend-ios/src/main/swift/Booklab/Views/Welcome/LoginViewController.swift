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

public class LoginViewController : AbstractWelcomeViewController {
    @IBOutlet var email: ErrorTextField!
    @IBOutlet var password: ErrorTextField!
    @IBOutlet var submit: RaisedButton!
    
    private var scope: String!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
       
        self.prepareEmail()
        self.preparePassword()
        self.prepareSubmit()
        
        self.scope = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_OAUTH_SCOPE") as! String
    }

	@IBAction public func login() {
        if email.isEmpty {
            email.detail = "Please enter your email address"
            email.isErrorRevealed = true
            return
        }
        
        if password.isEmpty {
            password.detail = "Please enter your password"
            password.isErrorRevealed = true
            return
        }
        
        // Resign the first responders
        self.view.endEditing(true)
	}
}

extension LoginViewController {
    fileprivate func prepareEmail() {
        self.prepareTextField(textField: email)
    }
    
    fileprivate func preparePassword() {
        self.prepareTextField(textField: password)
    }
    
    fileprivate func prepareSubmit() {
        self.prepareButton(button: submit)
    }
}

extension LoginViewController : TextFieldDelegate {
    public func textFieldDidEndEditing(_ textField: UITextField) {
        (textField as? ErrorTextField)?.isErrorRevealed = false
    }
    
    public func textFieldShouldClear(_ textField: UITextField) -> Bool {
        (textField as? ErrorTextField)?.isErrorRevealed = false
        return true
    }
    
    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let nextField = textField.superview?.viewWithTag(textField.tag + 1) as? UITextField {
            nextField.becomeFirstResponder()
        } else {
            // Not found, so remove keyboard.
            textField.resignFirstResponder()
        }
        return true
    }
}
