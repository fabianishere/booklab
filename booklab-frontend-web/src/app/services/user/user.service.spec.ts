import {UserService} from './user.service';
import {OAuthService} from "angular-oauth2-oidc";
import {Book} from "../../interfaces/user";
import {HttpService} from "../http/http.service";
import {Observable} from "rxjs/Rx";


describe('UserService should..', () => {
    let authSpy: jasmine.SpyObj<OAuthService>;
    let user: UserService;
    let httpSpy: jasmine.SpyObj<HttpService>;

    function initBookShelf(): Book[] {
        const book: Book = { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} };
        user.setBookshelf([book]);
        return [book];
    }

    beforeEach(() => {
        authSpy = jasmine.createSpyObj('OAuthService', ['hasValidAccessToken', 'fetchTokenUsingPasswordFlow', 'logOut']);
        httpSpy = jasmine.createSpyObj('HttpService', ['setCollection', 'addToCollection']);
        httpSpy.addToCollection.and.returnValue(new Observable());
        httpSpy.setCollection.and.returnValue(new Observable());
        authSpy.hasValidAccessToken.and.returnValue(true);
        user = new UserService(authSpy, jasmine.createSpyObj('Router', ['navigate']), httpSpy);
        user.collections = [{id: 1}];
    });

    it('be created', () => {
        expect(user).toBeTruthy();
    });

    it('return its bookshelf', () => {
        expect(user.getBookshelf()).toBeTruthy();
    });

    it('set its bookshelf', () => {
        let result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [
            { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} },
            { id: '1233', title: 'test2', authors: ['auth'], identifiers: { internal: '1233' }, categories: [], images: {} },
        ];
        user.setBookshelf(books);
        expect(result).toBe(books);
    });

    it('add a book to its bookshelf', () => {
        let result: Book[], content = initBookShelf();
        user.getBookshelf().subscribe(b => result = b);
        const book: Book = { id:' 123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} };
        user.addToBookshelf(book);
        expect(result).toEqual(content.concat([book]));
    });

    it('add multiple books to its bookshelf', () => {
        let content = initBookShelf(), result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [
            { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} },
            { id: '1233', title: 'test2', authors: ['auth'], identifiers: { internal: '1233' }, categories: [], images: {} },
        ];
        user.addMultToBookshelf(books);
        expect(result).toEqual(content.concat(books));
    });

    it('delete a book from its bookshelf', () => {
        let result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [
            { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} },
            { id: '1233', title: 'test2', authors: ['auth'], identifiers: { internal: '1233' }, categories: [], images: {} },
        ];
        user.setBookshelf(books);
        user.deleteFromBookshelf(books[1]);
        expect(result).toEqual([books[0]]);
    });

    it('try to login with given username and password', () => {
        user.login('test', 'password');
        expect(authSpy.fetchTokenUsingPasswordFlow.calls.count()).toBe(1);
        expect(authSpy.fetchTokenUsingPasswordFlow.calls.mostRecent().args).toEqual(['test', 'password']);
    });

    it('log out', () => {
        user.logout();
        expect(authSpy.logOut.calls.count()).toBe(1);

    });
});
