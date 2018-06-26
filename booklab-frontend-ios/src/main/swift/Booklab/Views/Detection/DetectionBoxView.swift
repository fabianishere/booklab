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
import ImageCoordinateSpace
import Material

public class DetectionBoxView : UIControl {
    public var detection: BookDetection!
    public var imageView: UIImageView!
    
    @IBOutlet public var label: UILabel!
    
    public var normalColor: UIColor = Color.green.base {
        didSet {
            setSelected(isSelected, animated: true)
        }
    }
    
    public var selectedColor: UIColor = Color.blue.base {
        didSet {
            setSelected(isSelected, animated: true)
        }
    }
    
    public override func awakeFromNib() {
        self.layer.borderWidth = 0.5
        self.isUserInteractionEnabled = true
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
    
        let coordinateSpace = imageView.contentSpace()
        self.frame = coordinateSpace.convert(detection.box.cgRect, to: imageView)
    }
    
    public func setSelected(_ selected: Bool, animated: Bool) {
        self.isSelected = selected
        let color = selected ? self.selectedColor : self.normalColor
        self.backgroundColor = color.withAlphaComponent(0.5)
        self.borderColor = color
    }
}
