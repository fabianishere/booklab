import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http/http.service";
import {UserService} from "../../services/user/user.service";
import {Book, BookItem} from "../../dataTypes";
import {isDefined} from "@angular/compiler/src/util";

@Component({
    selector: 'app-manual-book-find',
    templateUrl: './manual-book-find.component.html',
    styleUrls: ['./manual-book-find.component.less']
})
export class ManualBookFindComponent implements OnInit {

    public nameInput: string;
    public authorInput: string;
    public results: BookItem[];
    public searching = false;

    constructor(private user: UserService, private http: HttpService) {
    }

    ngOnInit() {
    }

    /**
     * Finds a book in the api with best corresponding title and author and adds it to the bookshelf.
     */
    findBook() {
        if (!this.nameInput || !this.authorInput) {
            return;
        }
        this.searching = true;
        this.results = null;
        this.http.findBook(this.nameInput, this.authorInput).subscribe((result) => {
            this.searching = false;
            this.results = result.map(r => new BookItem(r));
        }, error => this.http.handleError(error));
        this.authorInput = '';
        this.nameInput = '';

    }

    addManualToBookshelf() {
        this.results.filter(r => r.checked).forEach(r => {
            r.addedToShelf = true;
            this.user.addToBookshelf(r.book);
        });
    }

    deleteResult() {
        this.results = null;
    }

    booksAddedToShelf(): boolean {
        return isDefined(this.results.find(b => b.addedToShelf));
    }

}
