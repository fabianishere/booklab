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

@IBDesignable
public class DetectionCameraButton: UIButton {
    // Create a new layer to render the various circles
    var pathLayer:CAShapeLayer!
    let animationDuration = 0.2
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.prepare()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.prepare()
    }

    public override func awakeFromNib() {
        super.awakeFromNib()
        
        // lock the size to match the size of the camera button
        self.addConstraint(NSLayoutConstraint(item: self,
                                              attribute: .width,
                                              relatedBy: .equal,
                                              toItem: nil,
                                              attribute: .width,
                                              multiplier: 1,
                                              constant: 66.0))
        self.addConstraint(NSLayoutConstraint(item: self,
                                              attribute: .height,
                                              relatedBy: .equal,
                                              toItem: nil,
                                              attribute: .width,
                                              multiplier: 1,
                                              constant: 66.0))

        self.setTitle("", for:UIControlState.normal)
        
        self.addTarget(self, action: #selector(touchDown), for: UIControlEvents.touchDown)
        self.addTarget(self, action: #selector(touchUp), for: UIControlEvents.touchUpInside)
        self.addTarget(self, action: #selector(touchUp), for: UIControlEvents.touchUpOutside)
    }
    
    public override func layoutSubviews() {
        if self.pathLayer.path == nil {
            // show the right shape for the current state of the control
            self.pathLayer.path = largeCirclePath().cgPath
        }
    }
    
    public override func prepareForInterfaceBuilder() {
        self.setTitle("", for: UIControlState.normal)
    }
    
    
    fileprivate func prepare() {
        self.tintColor = UIColor.clear
        
        // add a shape layer for the inner shape to be able to animate it
        self.pathLayer = CAShapeLayer()
        
        // don't use a stroke color, which would give a ring around the inner circle
        self.pathLayer.strokeColor = nil
        
        // set the color for the inner shape
        self.pathLayer.fillColor = UIColor.white.cgColor
        
        // add the path layer to the control layer so it gets drawn
        self.layer.addSublayer(self.pathLayer)
    }
    
    
    @objc fileprivate func touchDown(sender: UIButton) {
        // change the inner shape to match the state
        let morph = CABasicAnimation(keyPath: "path")
        morph.duration = animationDuration;
        morph.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
        
        // change the shape according to the current state of the control
        morph.toValue = smallCirclePath().cgPath
        
        // ensure the animation is not reverted once completed
        morph.fillMode = kCAFillModeForwards
        morph.isRemovedOnCompletion = false
        
        // add the animation
        self.pathLayer.add(morph, forKey:"")
    }
    
    @objc fileprivate func touchUp(sender: UIButton) {
        // change the inner shape to match the state
        let morph = CABasicAnimation(keyPath: "path")
        morph.duration = animationDuration;
        morph.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
        
        // change the shape according to the current state of the control
        morph.toValue = largeCirclePath().cgPath
        
        // ensure the animation is not reverted once completed
        morph.fillMode = kCAFillModeForwards
        morph.isRemovedOnCompletion = false
        
        // add the animation
        self.pathLayer.add(morph, forKey:"")
    }
    
    public override func draw(_ rect: CGRect) {
        // always draw the outer ring, the inner control is drawn during the animations
        let outerRing = UIBezierPath(ovalIn: CGRect(x: 3, y: 3, width: 60, height: 60))
        outerRing.lineWidth = 6
        UIColor.white.setStroke()
        outerRing.stroke()
    }
    
    
    fileprivate func largeCirclePath() -> UIBezierPath {
        return UIBezierPath(
            arcCenter: CGPoint(x: bounds.width / 2, y: bounds.height / 2),
            radius: 25, startAngle: 0,
            endAngle: CGFloat(2 * Double.pi),
            clockwise: false
        )
        //return UIBezierPath(roundedRect: CGRect(x: 8, y: 8, width: 50, height: 50), cornerRadius: 25)
    }
    
    fileprivate func smallCirclePath() -> UIBezierPath {
        return UIBezierPath(
            arcCenter: CGPoint(x: bounds.width / 2, y: bounds.height / 2),
            radius: 22, startAngle: 0,
            endAngle: CGFloat(2 * Double.pi),
            clockwise: false
        )
    }
}
