import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ImageUploadComponent} from './image-upload.component';
import {HttpService} from "../../services/http/http.service";
import {UserService} from '../../services/user/user.service';
import {Observable} from "rxjs/Rx";
import {BookItem} from "../../dataTypes";

class MockEvent {
    public srcElement = new MockSourceElement();
}

class MockSourceElement {
    public files = [new Blob()];
}

describe('ImageUploadComponent should..', () => {
    let component: ImageUploadComponent;
    let http: jasmine.SpyObj<HttpService>;
    let user: jasmine.SpyObj<UserService>;


    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['putImg', 'checkHealth']);
        user = jasmine.createSpyObj('UserService', ['getBookshelf', 'addMultToBookshelf']);
        component = new ImageUploadComponent(http, user);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('send a picture to the httpservice', () => {
        http.putImg.and.returnValue(new Observable());
        component.onSubmit(new MockEvent());
        expect(http.putImg.calls.count()).toBe(1);
    });

    it('add found books to the bookshelf', () => {
        const books = [
            { title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} },
            { title: 'test2', authors: ['auth'], identifiers: { internal: '1233' }, categories: [], images: {} },
        ];
        component.results = books.map(b => new BookItem(b));
        component.addToBookShelf();
        expect(user.addMultToBookshelf.calls.count()).toBe(1);
        expect(user.addMultToBookshelf.calls.mostRecent().args[0]).toEqual(books);
    });

});
