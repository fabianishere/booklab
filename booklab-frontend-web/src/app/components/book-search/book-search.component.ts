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

import {Component, OnInit, ViewChild} from '@angular/core';
import {HttpService} from '../../services/http/http.service';
import {UserService} from '../../services/user/user.service';
import {Book, BookItem} from '../../dataTypes';
import {BooklistComponent} from "../booklist/booklist.component";
import {AddTo} from "../../interfaces";
import {Subject} from "rxjs/Rx";
import {ImageSearchComponent} from "../image-search/image-search.component";
import {PopupService} from "../../services/popup/popup.service";


@Component({
    selector: 'app-book-search',
    templateUrl: './book-search.component.html',
    styleUrls: ['./book-search.component.less']
})

/**
 * Class for the image upload component, handles the uploading of an image and can add it to the bookshelf.
 */
export class BookSearchComponent implements OnInit, AddTo {
    public enterBook = false;
    @ViewChild(BooklistComponent) booklist: BooklistComponent;
    @ViewChild(ImageSearchComponent) image: ImageSearchComponent;

    /**
     * Constructor for BookSearchComponent.
     * @param {HttpService} http
     * @param {UserService} user
     */
    constructor(private user: UserService, private popup: PopupService) {
    }

    ngOnInit() {
    }

    addTo(books: Book[]) {
        this.user.addMultToBookshelf(books);
        this.booklist.books = [];
        this.image.img = null;
        this.popup.openIsAdded();
    }

    onSubmit(event) {
        this.booklist.books = [];
        this.image
            .submit(event.srcElement.files[0])
            .subscribe(res => {
                this.booklist.books = res;
            });
    }
}
