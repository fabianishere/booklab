import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BookshelfComponent} from './bookshelf.component';
import {UserService} from "../../services/user/user.service";
import {FormsModule} from "@angular/forms";
import {HttpService} from "../../services/http/http.service";
import {Book, Title} from "../../dataTypes";
import {Observable} from "rxjs/Rx";

describe('BookshelfComponent should..', () => {
    let component: BookshelfComponent;
    let http: jasmine.SpyObj<HttpService>;
    let user: jasmine.SpyObj<UserService>;


    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['findBook']);
        user = jasmine.createSpyObj('UserService', ['deleteFromBookshelf','getBookshelf', 'addToBookshelf']);
        component = new BookshelfComponent(user, http);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('delete a book', () => {
        const book: Book = new Book([new Title('test', 'MAIN')], ['auth'], ['123']);
        component.deleteBook(book);
        expect(user.deleteFromBookshelf.calls.count()).toBe(1);
        expect(user.deleteFromBookshelf.calls.mostRecent().args[0]).toBe(book);
    });

    it('find a book', () => {
        http.findBook.and.returnValue(new Observable());
        component.authorInput = 'Willem Elsschot';
        component.nameInput = 'Kaas';
        component.findBook();
        expect(http.findBook.calls.count()).toBe(1);
        expect(http.findBook.calls.mostRecent().args).toEqual(['Kaas', 'Willem Elsschot']);
    });
});
