import {Component, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {Book, MockBook} from '../../dataTypes';
import {HttpService} from '../../services/http/http.service';

@Component({
    selector: 'app-bookshelf',
    templateUrl: './bookshelf.component.html',
    styleUrls: ['./bookshelf.component.less']
})
export class BookshelfComponent implements OnInit {

    public books: MockBook[];
    public enterBook: boolean;
    public nameInput: string;
    public authorInput: string;

    constructor(private user: UserService, private http: HttpService) {
    }

    ngOnInit() {
        this.user.getBookshelf().subscribe(b => {
            this.books = b;
        });
        this.enterBook = false;
    }

    delete(book: MockBook) {
        this.user.deleteFromBookshelf(book);
    }

    findBook() {
        this.user.addToBookshelf(new MockBook('', '', true))
        this.http.findBook(this.nameInput, this.authorInput).subscribe((book: Book[]) => {
            this.user.bookSearchComplete(new MockBook(book[0].ids[0], book[0].titles[0].value));
        });
        this.enterBook = false;
        this.authorInput = '';
        this.nameInput = '';

    }
}
