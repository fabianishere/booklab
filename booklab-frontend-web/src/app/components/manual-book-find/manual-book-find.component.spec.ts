import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ManualBookFindComponent} from './manual-book-find.component';
import {HttpService} from "../../services/http/http.service";
import {UserService} from "../../services/user/user.service";
import {Observable} from "rxjs/Rx";
import {Book, BookItem} from "../../interfaces/user";

describe('ManualBookFindComponent should..', () => {
    let component: ManualBookFindComponent;
    let http: jasmine.SpyObj<HttpService>;
    let user: jasmine.SpyObj<UserService>;

    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['findBook']);
        user = jasmine.createSpyObj('UserService', ['getBookshelf', 'addToBookshelf', 'addMultToBookshelf']);
        component = new ManualBookFindComponent(user, http);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('find a book', () => {
        http.findBook.and.returnValue(new Observable());
        component.authorInput = 'Willem Elsschot';
        component.nameInput = 'Kaas';
        component.findBook();
        expect(http.findBook.calls.count()).toBe(1);
        expect(http.findBook.calls.mostRecent().args).toEqual(['Kaas', 'Willem Elsschot']);
    });

    it('add a book to the bookshelf', () => {
        const books = [
            { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} },
            { id: '1233', title: 'test2', authors: ['auth'], identifiers: { internal: '1233' }, categories: [], images: {} },
        ];
        component.results = [new BookItem(books[0]), new BookItem(books[1])];
        component.addManualToBookshelf();
        expect(user.addMultToBookshelf.calls.count()).toBe(1);
        expect(user.addMultToBookshelf.calls.mostRecent().args[0]).toEqual(books);

    })
});
