import {Injectable} from '@angular/core';
import {Book} from '../../dataTypes';
import {Observable} from 'rxjs/Observable';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {OAuthService} from 'angular-oauth2-oidc';

@Injectable()
export class UserService {

    private bookshelf: Book[];
    private bookSub: BehaviorSubject<Book[]>;
    public loggedIn: boolean;


    constructor(private oauthService: OAuthService) {
        this.bookSub = new BehaviorSubject([]);
        this.bookshelf = [];
        this.loggedIn = this.oauthService.hasValidAccessToken();
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
        this.bookshelf = this.bookshelf.filter(b => b.getMainTitle() !== book.getMainTitle());
        this.bookSub.next(this.bookshelf);
    }

    bookSearchComplete(book: Book) {
        this.bookshelf[this.bookshelf.findIndex(b => b.isSearched)] = book;
    }

    login(username: string, password: string) {
        this.oauthService.fetchTokenUsingPasswordFlow('test@example.com', 'test').then((resp) => {
            if (resp) {
                this.loggedIn = true;
            }
        });
    }

    logout() {
        this.oauthService.logOut();
        this.loggedIn = false;
    }
}
