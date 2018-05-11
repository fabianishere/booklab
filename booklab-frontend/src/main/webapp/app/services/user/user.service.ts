import {Injectable} from '@angular/core';
import {Book} from '../../dataTypes';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class UserService {

    private bookshelf: Book[];
    private bookSub: BehaviorSubject<Book[]>;

    constructor() {
        this.bookSub = new BehaviorSubject([]);
        this.bookshelf = [];
    }

    getBookshelf(): Observable<Book[]> {
        return this.bookSub.asObservable();
    }

    setBookshelf(books: Book[]) {
        this.bookshelf = books;
        this.bookSub.next(this.bookshelf);
    }

    addToBookshelf(book: Book) {
        this.bookshelf.push(book);
        this.bookSub.next(this.bookshelf);
    }

    addMultToBookshelf(books: Book[]) {
        this.bookshelf = this.bookshelf.concat(books);
        this.bookSub.next(this.bookshelf);
    }


    deleteFromBookshelf(book: Book) {
        this.bookshelf = this.bookshelf.filter(b => b.isbn !== book.isbn);
        this.bookSub.next(this.bookshelf);
    }

}
