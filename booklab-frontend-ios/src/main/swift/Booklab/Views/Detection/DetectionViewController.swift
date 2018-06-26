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
import SceneKit
import ARKit
import URLNavigator
import Siesta
import Material

public class DetectionViewController: UIViewController, ARSCNViewDelegate {
    public var detectionService: DetectionService!
    
    @IBOutlet var sceneView: ARSCNView!
    @IBOutlet var preview: DetectionPreviewView!
   
    /**
     * Prefer a white status bat text since it matches the background better
     */
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Set the view's delegate
        sceneView.delegate = self
    }
    
    @IBAction func submit() {
        submit(image: sceneView.snapshot())
    }
    
    @IBAction func library() {
        let picker = UIImagePickerController()
        picker.sourceType = .photoLibrary
        picker.modalPresentationStyle = .overCurrentContext
        picker.delegate = self
        
        // We must present it via the navigation controller or otherwise our navigation
        // bar will overlap the bar of the picker view
        self.pulleyViewController?.navigationController?.present(picker, animated: true)
        
        // Fix for the preview failing on constraints
        self.preview.autoresizingMask = [.flexibleWidth, .flexibleHeight, .flexibleBottomMargin, .flexibleTopMargin]
    }
    
    /**
     * Submit the given image to the backend server.
     */
    public func submit(image: UIImage) {
        preview.imageView.image = image
        preview.isHidden = false
        sceneView.pause(self)

        let drawer = self.pulleyViewController?.drawerContentViewController as! DetectionDrawerViewController
        
        detectionService
            .detect(in: image)
            .useLoadingAlert(controller: self)
            .useSnackbar(snackbarController: self.snackbarController!)
            .onSuccess { entity in
                let results: [BookDetection] = entity.typedContent()!
               
                drawer.detections = results
                    .sorted { $0.box.x < $1.box.x }
                    .map { (detection: BookDetection) -> DetectionBoxView in
                        let view = Bundle.main.loadNibNamed("Detection", owner: drawer, options: nil)!.first as! DetectionBoxView
                        view.autoresizingMask = [.flexibleLeftMargin, .flexibleRightMargin, .flexibleBottomMargin, .flexibleTopMargin]
                        view.imageView = self.preview.imageView
                        view.detection = detection
                        view.normalColor = detection.matches.isEmpty ? Color.red.base : Color.green.base
                        view.selectedColor = Color.blue.base
                        view.label.text = ""
                        self.preview.addSubview(view)
                        return view
                    }
                    .filter { !$0.detection.matches.isEmpty }
                    .enumerated()
                    .map {
                        let (i, view) = $0
                        view.label.text = (i + 1).description
                        return view
                    }
                self.pulleyViewController?.setDrawerPosition(position: .partiallyRevealed, animated: true)
            }
            .onFailure { _ in
                self.reset()
            }
    }
    
    public func reset() {
        preview.isHidden = true
        preview.subviews.forEach {
            if $0 is DetectionBoxView {
                $0.removeFromSuperview()
            }
        }
        sceneView.play(self)
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Create a session configuration
        let configuration = ARWorldTrackingConfiguration()
        configuration.planeDetection = .horizontal

        // Run the view's session
        sceneView.session.run(configuration)
    }
    
    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Pause the view's session
        sceneView.session.pause()
    }
}

extension DetectionViewController : UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        submit(image: image)
        picker.dismiss(animated: true, completion: nil)
    }
    
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
}

extension Request {
    /**
     * This method shows a loading alert while the request is in progress.
     */
    func useLoadingAlert(controller: UIViewController, message: String = "Please wait...") -> Request {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        let loadingIndicator = UIActivityIndicatorView(frame: CGRect(x: 10, y: 5, width: 50, height: 50))
        
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.gray
        loadingIndicator.startAnimating();
        
        alert.view.addSubview(loadingIndicator)
        controller.present(alert, animated: true, completion: nil)
        
        return onCompletion { _ in alert.dismiss(animated: true) }
    }
}
