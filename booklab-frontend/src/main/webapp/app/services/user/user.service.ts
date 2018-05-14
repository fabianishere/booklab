import {Injectable} from '@angular/core';
import {MockBook} from '../../dataTypes';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class UserService {

    private bookshelf: MockBook[];
    private bookSub: BehaviorSubject<MockBook[]>;

    constructor() {
        this.bookSub = new BehaviorSubject([]);
        this.bookshelf = [];
    }

    getBookshelf(): Observable<MockBook[]> {
        return this.bookSub.asObservable();
    }

    setBookshelf(books: MockBook[]) {
        this.bookshelf = books;
        this.bookSub.next(this.bookshelf);
    }

    addToBookshelf(book: MockBook) {
        this.bookshelf.push(book);
        this.bookSub.next(this.bookshelf);
    }

    addMultToBookshelf(books: MockBook[]) {
        this.bookshelf = this.bookshelf.concat(books);
        this.bookSub.next(this.bookshelf);
    }


    deleteFromBookshelf(book: MockBook) {
        this.bookshelf = this.bookshelf.filter(b => b.isbn !== book.isbn);
        this.bookSub.next(this.bookshelf);
    }

    bookSearchComplete(mockBook: MockBook) {
        this.bookshelf[this.bookshelf.findIndex(b => b.isSearched)] = mockBook;
    }
}
