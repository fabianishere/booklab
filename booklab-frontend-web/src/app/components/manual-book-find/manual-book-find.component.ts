import {AfterViewChecked, Component, OnInit, ViewChild} from '@angular/core';
import {HttpService} from "../../services/http/http.service";
import {UserService} from "../../services/user/user.service";
import {Book, BookItem} from "../../dataTypes";
import {isDefined} from "@angular/compiler/src/util";
import {PopupService} from "../../services/popup/popup.service";

@Component({
    selector: 'app-manual-book-find',
    templateUrl: './manual-book-find.component.html',
    styleUrls: ['./manual-book-find.component.less']
})
export class ManualBookFindComponent implements OnInit, AfterViewChecked {

    public nameInput: string;
    public authorInput: string;
    public results: BookItem[];
    public searching = false;
    public scrolled = false;
    public notFound = false;

    constructor(private user: UserService, private http: HttpService, private popup: PopupService) {
    }

    ngOnInit() {
        this.results = [];
    }

    ngAfterViewChecked() {
        const el = document.getElementById('search');
        if (!this.scrolled && el) {
            el.scrollIntoView();
        }
    }

    /**
     * Finds a book in the api with best corresponding title and author and adds it to the bookshelf.
     */
    findBook() {
        if (!this.nameInput || !this.authorInput) {
            return;
        }
        this.notFound = false;
        this.searching = true;
        this.results = [];
        this.http.findBook(this.nameInput, this.authorInput).subscribe((result) => {
            this.searching = false;
            this.results = result.map(r => new BookItem(r));
            this.notFound = result.length == 0;
        }, error => this.http.handleError(error));
        this.authorInput = '';
        this.nameInput = '';


    }

    addManualToBookshelf() {
        this.results.filter(r => r.checked).forEach(r => {
            r.added = true;
            this.user.addToBookshelf(r.book);
        });
        this.popup.opendIsAdded();
        this.results = [];
    }

    reset() {
        this.results = [];
    }

    booksAddedToShelf(): boolean {
        return isDefined(this.results.find(b => b.added));
    }
}
