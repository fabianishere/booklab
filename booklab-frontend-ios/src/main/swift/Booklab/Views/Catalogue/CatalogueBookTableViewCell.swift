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

public class CatalogueBookTableViewCell : UITableViewCell {
    @IBOutlet var title: UILabel!
    @IBOutlet var author: UILabel!
    @IBOutlet var cover: RemoteImageView!
    @IBOutlet var ratings: CosmosView?
    
    public var book: Book! {
        didSet {
            let placeholder = UIImage(named: "BookThumbnail")
            title.text = book.title
            author.text = book.authors.count > 0 ? book.authors[0] : "Unknown Author"
            cover.imageURL = book.images["thumbnail"]?.absoluteString
            cover.placeholderImage = placeholder
            cover.loadingView = UIImageView(image: placeholder)
            ratings?.rating = book.ratings?.average ?? 0
            ratings?.text = "(\(book.ratings?.count ?? 0))"
        }
    }
}
