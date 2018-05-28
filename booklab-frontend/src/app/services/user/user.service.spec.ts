import {UserService} from './user.service';
import {OAuthService} from "angular-oauth2-oidc";
import {Observable} from "rxjs/Observable";
import {Book, Title} from "../../dataTypes";


describe('UserService should..', () => {
    let authSpy: jasmine.SpyObj<OAuthService>;
    let user: UserService;

    function initBookShelf(): Book[] {
        const book = new Book([new Title('test', 'MAIN')], ['auth'], ['123']);
        user.setBookshelf([book]);
        return [book];
    }

    beforeEach(() => {
        authSpy = jasmine.createSpyObj('OAuthService', ['hasValidAccessToken', 'fetchTokenUsingPasswordFlow', 'logOut']);
        authSpy.hasValidAccessToken.and.returnValue(true);
        user = new UserService(authSpy);
    });

    it('be created', () => {
        expect(user).toBeTruthy();
    });

    it('return its bookshelf', () => {
        expect(user.getBookshelf()).toBeTruthy()
    });

    it('set its bookshelf', () => {
        let result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [new Book([new Title('test', 'MAIN')], ['auth'], ['123']),
            new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])];
        user.setBookshelf(books);
        expect(result).toBe(books);
    });

    it('add a book to its bookshelf', () => {
        let result: Book[], content = initBookShelf();
        user.getBookshelf().subscribe(b => result = b);
        const book = new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223']);
        user.addToBookshelf(book);
        expect(result).toEqual(content.concat([book]));
    });

    it('add multiple books to its bookshelf', () => {
        let content = initBookShelf(), result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [new Book([new Title('test', 'MAIN')], ['auth'], ['123']),
            new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])];
        user.addMultToBookshelf(books);
        expect(result).toEqual(content.concat(books));
    });

    it('delete a book from its bookshelf', () => {
        let result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        const books = [new Book([new Title('test', 'MAIN')], ['auth'], ['123']),
            new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])];
        user.setBookshelf(books);
        user.deleteFromBookshelf(new Book([new Title('test', 'MAIN')], ['auth'], ['123']));
        expect(result).toEqual([new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])]);
    });

    it('replace a searched book with its found values', () => {
        let result: Book[];
        user.getBookshelf().subscribe(b => result = b);
        user.addToBookshelf(new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'], true));
        const found = new Book([new Title('found', 'MAIN')], ['author'], ['waddup']);
        user.bookSearchComplete(found);
        expect(result).toEqual([found]);
    });

    it('try to login with given usernamen and password', () => {
        user.login('test', 'password');
        expect(authSpy.fetchTokenUsingPasswordFlow.calls.count()).toBe(1);
        expect(authSpy.fetchTokenUsingPasswordFlow.calls.mostRecent().args).toEqual(['test', 'password']);
    });

    it('log out', () => {
        user.logout();
        expect(authSpy.logOut.calls.count()).toBe(1);

    });

});
