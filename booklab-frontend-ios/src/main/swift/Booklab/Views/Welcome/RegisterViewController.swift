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

public class RegisterViewController : AbstractWelcomeViewController {
    public var navigator: NavigatorType!
    public var authorizationService: AuthorizationService!
    public var userService: UserService!
    
    @IBOutlet var email: ErrorTextField!
    @IBOutlet var password: ErrorTextField!
    @IBOutlet var confirmation: ErrorTextField!
    @IBOutlet var submit: RaisedButton!
    
    private var scope: String!
    private var isLoading: Bool = false
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.prepareEmail()
        self.preparePassword()
        self.prepareConfirmation()
        self.prepareSubmit()
        
        self.scope = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_OAUTH_SCOPE") as! String
    }
    
    @IBAction public func register() {
        // Prevent button smashers from sending multiple requests while loading
        if isLoading {
            return
        }
        
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
        
        if confirmation.isEmpty {
            password.detail = "Please repeat your password"
            password.isErrorRevealed = true
            return
        }
        
        
        // Resign the first responders
        self.view.endEditing(true)
        
        // Make the request to register the user
        register(email: self.email.text!, password: self.password.text!)
    }
    
    func register(email: String, password: String) {
        isLoading = true
        // We need a custom OAuth scope for this, so request that first
        authorizationService
            .authorize(scope: "user:registration")
            .flatMap { entity in
                let token = entity.content as! AuthorizationToken
                return self.userService.register(email: email, password: password) {
                    $0.addValue("Bearer \(token.value)", forHTTPHeaderField: "Authorization")
                }
            }
            .flatMap { _ in
                let request = self.authorizationService.authorize(
                    username: email,
                    password: password,
                    scope: self.scope
                )
                return self.authorizationService.token.load(using: request)
            }
            .flatMap { entity in
                // Store the token in the user defaults
                let token: AuthorizationToken = entity.typedContent()!
                UserDefaults.standard.set(try? PropertyListEncoder().encode(token), forKey: "booklab-oauth-token")
                
                // Reload the user profile
                return self.userService.me.load()
            }
            .useSnackbar(snackbarController: self.snackbarController!)
            .onSuccess { _ in
                self.navigator.present("/")
            }
            .onCompletion { _ in
                self.isLoading = false
            }
    }
}

extension RegisterViewController {
    fileprivate func prepareEmail() {
        self.prepareTextField(textField: email)
    }
    
    fileprivate func preparePassword() {
        self.prepareTextField(textField: password)
    }
    
    fileprivate func prepareConfirmation() {
        self.prepareTextField(textField: confirmation)
    }
    
    fileprivate func prepareSubmit() {
        self.prepareButton(button: submit)
    }
}

extension RegisterViewController : TextFieldDelegate {
    public func textFieldDidEndEditing(_ textField: UITextField) {
        (textField as? ErrorTextField)?.isErrorRevealed = false
        let _ = validateField(textField: textField)
    }
    
    
    private func isValidEmail(test: String) -> Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: test)
    }
    
    public func textFieldShouldClear(_ textField: UITextField) -> Bool {
        (textField as? ErrorTextField)?.isErrorRevealed = false
        return true
    }
    
    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if !validateField(textField: textField) {
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
    
    private func validateField(textField: UITextField) -> Bool {
        if textField == email && !email.isEmpty && !isValidEmail(test: email.text!) {
            email.detail = "Please enter a valid email address"
            email.isErrorRevealed = true
            return false
        }
        
        if textField == password && !password.isEmpty && password.text!.count < 5 {
            password.detail = "The password should contain at least 5 characters"
            password.isErrorRevealed = true
            return false
        }
        
        if textField == confirmation && !confirmation.isEmpty && password.text != confirmation.text {
            confirmation.detail = "The two passwords do not match"
            confirmation.isErrorRevealed = true
            return false
        }
        
        return true
    }
}
