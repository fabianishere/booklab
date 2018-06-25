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
import Cosmos
import SiestaUI

public class CatalogueBookViewController : UIViewController {
    @IBOutlet var coverView: RemoteImageView!
    @IBOutlet var authorLabel: UILabel!
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var ratingsView: CosmosView!
    @IBOutlet var descriptionView: UITextView!
    
    public var book: Book! {
        didSet {
            if isViewLoaded {
                prepare()
            }
        }
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        self.prepare()
    }
    
    fileprivate func prepare() {
        titleLabel.text = book.title
        authorLabel.text = book.authors.count > 0 ? book.authors.joined(separator: ",") : "Unknown Author"
        coverView.imageURL = book.images["thumbnail"]?.absoluteString
        coverView.placeholderImage = UIImage(named: "BookThumbnail")
        ratingsView.rating = book.ratings?.average ?? 0
        ratingsView.text = "(\(book.ratings?.count ?? 0))"
        descriptionView.text = book.description
    }
}
