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

public class DetectionPulleyViewController : PulleyViewController {
    
    public override func viewDidLoad() {
        self.initialDrawerPosition = .closed
        super.viewDidLoad()
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        // Prevent the drawer from jumping when the view appears
        self.setDrawerPosition(position: self.drawerPosition, animated: false)
        super.viewWillAppear(animated)
    }
}
