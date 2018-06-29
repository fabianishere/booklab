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

public class ApplicationTabsController: TabsController {
    
    var navigator: NavigatorType!
    
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        return viewControllers.count > selectedIndex ? viewControllers[selectedIndex].preferredStatusBarStyle : .default
    }
    
    public override func viewDidLoad() {
        viewControllers = [
            navigator.viewController(for: "/explore")!,
            navigator.viewController(for: "/detection")!,
            navigator.viewController(for: "/collections")!,
            navigator.viewController(for: "/settings")!,
        ];
        
        super.viewDidLoad()
    }
    
    public override func prepare() {
        super.prepare()
        
        isMotionEnabled = true
        tabBar.isDividerHidden = true
        tabBar.lineAlignment = .bottom
        tabBar.lineColor = Color.pink.base
        tabBar.tintColor = Color.pink.base
        tabBar.tabBarStyle = .auto
        tabBar.motionIdentifier = "options"
    }
}
