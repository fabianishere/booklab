import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http/http.service";
import {UserService} from "../../services/user/user.service";
import {Book, BookItem} from "../../dataTypes";

@Component({
    selector: 'app-manual-book-find',
    templateUrl: './manual-book-find.component.html',
    styleUrls: ['./manual-book-find.component.less']
})
export class ManualBookFindComponent implements OnInit {

    public nameInput: string;
    public authorInput: string;
    public result: BookItem;
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
        this.http.findBook(this.nameInput, this.authorInput).subscribe((result) => {
            this.searching = false;
            this.result = new BookItem(Book.getBook(result.results[0]));
        }, error => this.http.handleError(error));
        this.authorInput = '';
        this.nameInput = '';

    }

    addManualToBookshelf() {
        this.result.addedToShelf = true;
        this.user.addToBookshelf(this.result.book);
    }

    deleteResult() {
        this.result = null;
    }

}
