import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ImageUploadComponent} from './image-upload.component';
import {HttpService} from "../../services/http/http.service";
import {UserService} from '../../services/user/user.service';
import {Observable} from "rxjs/Rx";
import {Book, Title} from "../../dataTypes";

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
       let books = [new Book([new Title('test', 'MAIN')], ['auth'], ['123']),
           new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])];
        component.results = books;
        component.addToBookShelf();
       expect(user.addMultToBookshelf.calls.count()).toBe(1);
       expect(user.addMultToBookshelf.calls.mostRecent().args[0]).toBe(books);
    });

});
