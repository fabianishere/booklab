import {Injectable} from '@angular/core';
import {Book} from "../../dataTypes";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {BehaviorSubject} from "rxjs/BehaviorSubject";

@Injectable()
export class UserService {

    private bookshelf: Book[];
    private bookObs: BehaviorSubject<Book[]>;

    constructor() {
        this.bookObs = new BehaviorSubject([]);
    }

    getBookshelf(): Observable<Book[]> {
        return this.bookObs.asObservable();
    }

    setBookshelf(books: Book[]) {
        this.bookshelf = books;
        this.bookObs.next(this.bookshelf);
    }

    addToBookshelf(book: Book) {
        this.bookshelf.push(book);
        this.bookObs.next(this.bookshelf);
    }

    deleteFromBookshelf(book: Book) {
        this.bookshelf = this.bookshelf.filter(b => b.isbn!=book.isbn);
        this.bookObs.next(this.bookshelf);
    }

}
