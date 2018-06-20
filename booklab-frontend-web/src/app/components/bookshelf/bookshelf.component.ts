import {Component, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {Book} from '../../interfaces/user';
import {HttpService} from '../../services/http/http.service';

@Component({
    selector: 'app-bookshelf',
    templateUrl: './bookshelf.component.html',
    styleUrls: ['./bookshelf.component.less']
})

export class BookshelfComponent implements OnInit {

    public books: Book[];

    /**
     * Constructor for bookshelf component
     * @param {UserService} user
     */
    constructor(private user: UserService) {
    }

    ngOnInit() {
        this.user.getBookshelf().subscribe(b => {
            this.books = b;
        });
    }

    /**
     * Deletes book from the users bookshelf.
     * @param {Book} book: book to be deleted
     */
    deleteBook(book: Book) {
        this.user.deleteFromBookshelf(book);
    }

}
