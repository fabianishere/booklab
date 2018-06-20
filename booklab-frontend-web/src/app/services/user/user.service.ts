import {Injectable} from '@angular/core';
import {Book, BookCollection} from '../../interfaces/user';
import {Observable} from 'rxjs/Observable';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {OAuthService} from 'angular-oauth2-oidc';
import {CanActivate, Router} from "@angular/router";
import {HttpService} from "../http/http.service";
import {isUndefined} from "util";

/**
 * Class to hold the information of the user and to provide an interface for editing this information.
 */
@Injectable()
export class UserService implements CanActivate{

    private bookshelf: Book[];
    private bookSub: BehaviorSubject<Book[]>;
    private id: number;
    loggedIn: boolean;

    /**
     * Constructor for UserService.
     *
     * @param oauthService The OAuth service provider to use.
     * @param router
     * @param http
     */
    constructor(private oauthService: OAuthService, private router: Router, private http: HttpService) {
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

    loadUser() {
        this.http.getUser('1').subscribe(res => {
            if(!isUndefined(res.collections)){
                this.setBookshelf(res.collections.reduce((total: Book[], curr: BookCollection) => total.concat(curr.books), []));
            }
            this.id = res.id;
        }, err => this.http.handleError(err));
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
        this.bookshelf = this.bookshelf.filter(b => b.title !== book.title);
        this.bookSub.next(this.bookshelf);
    }

    login(username: string, password: string): Promise<Object> {
        return this.oauthService.fetchTokenUsingPasswordFlow(username, password);
    }

    logout() {
        this.oauthService.logOut();
        this.loggedIn = false;
        this.bookshelf = [];
        this.bookSub.next([]);
        this.router.navigate([""]);
    }
}
