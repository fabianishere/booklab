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

import Material
import UIKit
import Siesta

public class ApplicationSnackbarController : SnackbarController {
    fileprivate var dismissButton: FlatButton!
    
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        return rootViewController.preferredStatusBarStyle
    }
    
    public override var prefersStatusBarHidden: Bool {
        return rootViewController.prefersStatusBarHidden
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.prepareDismissButton()
    }
}

extension ApplicationSnackbarController {
    fileprivate func prepareDismissButton() {
        dismissButton = FlatButton(title: "Dismiss", titleColor: Color.pink.base)
        dismissButton.pulseAnimation = .backing
        dismissButton.titleLabel?.font = snackbarController?.snackbar.textLabel.font
        dismissButton.addTarget(self, action: #selector(dismissSnackbar(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func dismissSnackbar(_ sender: Any) {
        snackbarController?.animate(snackbar: .hidden)
    }
}

extension SnackbarController {
    public func toast(text: String) {
        snackbar.text = text
        
        if let applicationSnackbarController = self as? ApplicationSnackbarController {
            snackbar.rightViews = [applicationSnackbarController.dismissButton]
        }
        
        animate()
    }
    
    public func animate() {
        _ = animate(snackbar: .visible, delay: 0)
        _ = animate(snackbar: .hidden, delay: 4)
    }
}

extension SnackbarController : ResourceObserver {
    public func resourceChanged(_ resource: Resource, event: ResourceEvent) {
        switch event {
        case .error:
            let error = resource.latestError!
            toast(text: error.cause?.localizedDescription ?? error.localizedDescription)
        default:
            return
        }
    }
}

public extension Request {
    /**
     * Use the [SnackbarController] for logging the request failures.
     */
    public func useSnackbar(snackbarController: SnackbarController) -> Request {
        return onFailure { error in
            snackbarController.toast(text: error.cause?.localizedDescription ?? error.localizedDescription)
        }
    }
}
