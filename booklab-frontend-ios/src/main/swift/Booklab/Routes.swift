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

import Swinject
import SwinjectStoryboard
import SwinjectAutoregistration
import URLNavigator

/**
 * This structure contains the routes of the application
 */
struct Routes {
    /**
     * Initialise the given [NavigatorType].
     */
    static func initialize(container: Container, navigator: NavigatorType) {
        let welcome = container.resolve(SwinjectStoryboard.self, name: "Welcome")!
        let main = container.resolve(SwinjectStoryboard.self, name: "Main")!
    
        navigator.register("/welcome") { _, _, _ in
            if let welcome = welcome.instantiateInitialViewController() {
                return ApplicationSnackbarController(rootViewController: welcome)
            }
            return nil
        }
        
        navigator.register("/") { _, _, _ in
            if let main = main.instantiateInitialViewController() {
                return ApplicationSnackbarController(rootViewController: main)
            }
            return nil
        }
        
        navigator.register("/explore") { _, _, _ in
            main.instantiateViewController(withIdentifier: "explore")
        }
        
        navigator.register("/catalogue/<id:int>") { url, values, context in
            main.instantiateViewController(withIdentifier: "catalogue-child")
        }
        
        navigator.register("/catalogue") { url, values, context in
            let controller = main.instantiateViewController(withIdentifier: "catalogue-root") as? CatalogueSearchTableViewController
            controller?.searchText = context as? String
            return controller
        }
        
        navigator.register("/detection") { _, _, _ in
            main.instantiateViewController(withIdentifier: "detection")
        }
        
        navigator.register("/collections") { _, _, _ in
            main.instantiateViewController(withIdentifier: "collections")
        }
        
        navigator.register("/settings") { _, _, _ in
            main.instantiateViewController(withIdentifier: "settings")
        }
    }
}
