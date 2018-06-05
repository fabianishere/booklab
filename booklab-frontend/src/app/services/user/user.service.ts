import {Injectable} from '@angular/core';
import {Book} from '../../dataTypes';
import {Observable} from 'rxjs/Observable';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {OAuthService} from 'angular-oauth2-oidc';
import {CanActivate, Router} from "@angular/router";

/**
 * Class to hold the information of the user and to provide an interface for editing this information.
 */
@Injectable()
export class UserService implements CanActivate{

    private bookshelf: Book[];
    private bookSub: BehaviorSubject<Book[]>;
    public loggedIn: boolean;

    /**
     * Constructor for UserService.
     *
     * @param oauthService The OAuth service provider to use.
     */
    constructor(private oauthService: OAuthService, private router: Router) {
        this.bookSub = new BehaviorSubject([]);
        this.bookshelf = [];
        this.loggedIn = this.oauthService.hasValidAccessToken();
    }

    canActivate() {
        if(this.oauthService.hasValidAccessToken()) {
            return true;
        }
        else {
            this.router.navigate([""]);
        }
    }

    /**
     * Method to get an subscription to the bookshelf.
     * @returns {Observable<Book[]>}: observable of the users bookshelf
     */
    getBookshelf(): Observable<Book[]> {
        return this.bookSub.asObservable();
    }

    /**
     * Sets the bookshelf of the user.
     * @param {Book[]} books: new bookshelf of the user
     */
    setBookshelf(books: Book[]) {
        this.bookshelf = books;
        this.bookSub.next(this.bookshelf);
    }

    /**
     * Adds a book to the bookshelf of the user.
     * @param {Book} book: book to be added
     */
    addToBookshelf(book: Book) {
        this.bookshelf.push(book);
        this.bookSub.next(this.bookshelf);
    }

    /**
     * Adds multiple books to the bookshelf of the user.
     * @param {Book[]} books
     */
    addMultToBookshelf(books: Book[]) {
        this.bookshelf = this.bookshelf.concat(books);
        this.bookSub.next(this.bookshelf);
    }


    /**
     * Deletes a book from the bookshelf.
     * @param {Book} book: book to be deleted
     */
    deleteFromBookshelf(book: Book) {
        this.bookshelf = this.bookshelf.filter(b => b.getMainTitle() !== book.getMainTitle());
        this.bookSub.next(this.bookshelf);
    }

    /**
     * Sets a book that still was being searched to its found values.
     * @param {Book} book: book that was found.
     */
    bookSearchComplete(book: Book) {
        this.bookshelf[this.bookshelf.findIndex(b => b.isSearched)] = book;
    }

    login(username: string, password: string): Promise<Object> {
        return this.oauthService.fetchTokenUsingPasswordFlow(username, password);
    }

    logout() {
        this.oauthService.logOut();
        this.loggedIn = false;
        this.router.navigate([""]);
    }
}
