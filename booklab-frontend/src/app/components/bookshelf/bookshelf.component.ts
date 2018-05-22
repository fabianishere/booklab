import {Component, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {Book, Title} from '../../dataTypes';
import {HttpService} from '../../services/http/http.service';

@Component({
    selector: 'app-bookshelf',
    templateUrl: './bookshelf.component.html',
    styleUrls: ['./bookshelf.component.less']
})

export class BookshelfComponent implements OnInit {

    public books: Book[];
    public enterBook: boolean;
    public nameInput: string;
    public authorInput: string;

    /**
     * Constructor for bookshelf component, handles user-input to the bookshelf.
     *
     * @param {UserService} user
     * @param {HttpService} http
     */
    constructor(private user: UserService, private http: HttpService) {
    }

    ngOnInit() {
        this.user.getBookshelf().subscribe(b => {
            this.books = b;
        });
        this.enterBook = false;
    }

    /**
     * Deletes book from the users bookshelf.
     * @param {Book} book: book to be deleted
     */
    deleteBook(book: Book) {
        this.user.deleteFromBookshelf(book);
    }

    /**
     * Finds a book in the api with best corresponding title and author and adds it to the bookshelf.
     */
    findBook() {
        this.user.addToBookshelf(new Book([], [], [], true));
        this.http.findBook(this.nameInput, this.authorInput).subscribe((result) => {
            this.user.bookSearchComplete(Book.getBook(result.results[0]));
        });
        this.enterBook = false;
        this.authorInput = '';
        this.nameInput = '';

    }
}
