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
import URLNavigator
import Swinject
import SwinjectAutoregistration
import SwinjectStoryboard

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

	var window: UIWindow?
    
    /* The dependency injection container of this application */
    let container: Container = {
        let container = Container()
        
        // URLNavigator
        container
            .register(NavigatorType.self) { _ in Navigator() }
            .inObjectScope(.container)
        

        // Section: REST Configuration
        container
            .register(AuthorizationService.self) { r in
                let baseUrl: String = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_BASE_URL") as! String
                let id: String = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_OAUTH_ID") as! String
                let secret: String = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_OAUTH_SECRET") as! String
                
                
                return AuthorizationService(
                    baseUrl: "\(baseUrl)/auth",
                    clientId: id,
                    clientSecret: secret
                )
            }
            .inObjectScope(.container)
        
        container
            .register(BackendService.self) { r in
                let baseUrl: String = Bundle.main.object(forInfoDictionaryKey: "BOOKLAB_API_BASE_URL") as! String
                return BackendService(authorization: r~>, baseUrl: baseUrl)
            }
            .inObjectScope(.container)
        container
            .register(UserService.self) { r in
                let backend = r ~> BackendService.self
                return UserService(resource: backend.users)
            }
            .inObjectScope(.container)
        container
            .register(CatalogueService.self) { r in
                let backend = r ~> BackendService.self
                return CatalogueService(resource: backend.catalogue)
            }
            .inObjectScope(.container)
        container
            .register(DetectionService.self) { r in
                let backend = r ~> BackendService.self
                return DetectionService(resource: backend.detection)
            }
            .inObjectScope(.container)
        container
            .register(CollectionService.self) { r in
                let backend = r ~> BackendService.self
                return CollectionService(resource: backend.collections)
            }
            .inObjectScope(.container)
        container
            .register(RecommendationService.self) { r in
                let backend = r ~> BackendService.self
                return RecommendationService(resource: backend.recommendations)
            }
            .inObjectScope(.container)

        // Section: Storyboards
        container
            .register(SwinjectStoryboard.self, name: "Welcome") { r in SwinjectStoryboard.create(name: "Welcome", bundle: nil, container: r) }
            .inObjectScope(.container)
        
        container
            .register(SwinjectStoryboard.self, name: "Main") { r in SwinjectStoryboard.create(name: "Main", bundle: nil, container: r) }
            .inObjectScope(.container)
        
        // Storyboard controller configurations
        container.storyboardInitCompleted(ApplicationTabsController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
        }
        
        // SECTION: Welcome
        container.storyboardInitCompleted(LoginViewController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
            c.authorizationService = r ~> AuthorizationService.self
            c.userService = r ~> UserService.self
        }
        container.storyboardInitCompleted(RegisterViewController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
            c.authorizationService = r ~> AuthorizationService.self
            c.userService = r ~> UserService.self
        }
        
        // SECTION: Explore
        container.storyboardInitCompleted(ExploreViewController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
            c.catalogueService = r ~> CatalogueService.self
        }
        
        // SECTION: Settings
        container.storyboardInitCompleted(SettingsViewController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
            c.authorizationService = r ~> AuthorizationService.self
        }
        
        // SECTION: Catalogue
        container.storyboardInitCompleted(CatalogueSearchTableViewController.self) { r, c in
            c.catalogueService = r ~> CatalogueService.self
        }
        
        // SECTION: Detection
        container.storyboardInitCompleted(DetectionViewController.self) { r, c in
            c.detectionService = r ~> DetectionService.self
        }
        container.storyboardInitCompleted(DetectionDrawerViewController.self) { r, c in
            c.navigator = r ~> NavigatorType.self
            c.collectionService = r ~> CollectionService.self
        }
        
        // SECTION: Collection
        container.storyboardInitCompleted(CollectionTableViewController.self) { r, c in
            c.userService = r ~> UserService.self
            c.collectionService = r ~> CollectionService.self
        }
        container.storyboardInitCompleted(CollectionSelectionViewController.self) { r, c in
            c.userService = r ~> UserService.self
            c.collectionService = r ~> CollectionService.self
        }
        container.storyboardInitCompleted(CollectionSelectionAddToViewController.self) { r, c in
            c.userService = r ~> UserService.self
            c.collectionService = r ~> CollectionService.self
        }
        
        // SECTION: Recommendation
        container.storyboardInitCompleted(RecommendationSelectionViewController.self) { r, c in
            c.userService = r ~> UserService.self
            c.collectionService = r ~> CollectionService.self
            c.recommendationService = r ~> RecommendationService.self
        }
        
        return container
    }()
    

	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // The navigator to use
        let navigator = container ~> NavigatorType.self
        
        // Initialise the routes of the application
        Routes.initialize(container: container, navigator: navigator)
        
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.makeKeyAndVisible()
        self.window = window
        
        let controller: UIViewController
        
        // Load token from user defaults if not expired yet
        // Otherwise, we present a login view
        if let data = UserDefaults.standard.object(forKey: "booklab-oauth-token") as? Data,
           let token = try? PropertyListDecoder().decode(AuthorizationToken.self, from: data), !token.isExpired {
            let authorizationService = container ~> AuthorizationService.self
            authorizationService.token.overrideLocalContent(with: token)
            controller = navigator.viewController(for: "/")!
        } else {
            controller = navigator.viewController(for: "/welcome")!
        }

        window.rootViewController = controller
      
		return true
	}
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey: Any]) -> Bool {
        let navigator = container ~> NavigatorType.self
        // Try presenting the URL first
        if navigator.present(url, wrap: UINavigationController.self) != nil {
            return true
        }
        
        // Try opening the URL
        if navigator.open(url) == true {
            return true
        }

        return false
    }

	func applicationWillResignActive(_ application: UIApplication) {
		// Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
		// Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
	}

	func applicationDidEnterBackground(_ application: UIApplication) {
		// Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
		// If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
	}

	func applicationWillEnterForeground(_ application: UIApplication) {
		// Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
	}

	func applicationDidBecomeActive(_ application: UIApplication) {
		// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
	}

	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
	}
}
