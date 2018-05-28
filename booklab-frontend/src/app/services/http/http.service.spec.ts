import {TestBed, inject} from '@angular/core/testing';

import {HttpService} from './http.service';
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Rx";

describe('HttpService should..', () => {
    let httpCSpy: jasmine.SpyObj<HttpClient>;
    let routerSpy: jasmine.SpyObj<Router>;
    let http: HttpService;

    beforeEach(() => {
        httpCSpy = jasmine.createSpyObj('HttpClient', ['post', 'get']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);
        http = new HttpService(httpCSpy, routerSpy);
    });

    it('be created', () => {
        expect(http).toBeTruthy();
    });

    it('post an image to the backend', () => {
        http.putImg(null);
        expect(httpCSpy.post.calls.count()).toBe(1);
    });

    it('send a message to the backend to find a book', () => {
        http.findBook('testname', 'testAuthor');
        expect(httpCSpy.get.calls.mostRecent().args[0]
            .split(new RegExp('title=|&author=')).slice(1, 3))
            .toEqual(['testname', 'testAuthor']);
        expect(httpCSpy.get.calls.count()).toBe(1);
    });

    it('navigate to sorry page when called upon', () => {
        http.handleError(null);
        expect(routerSpy.navigate.calls.count()).toBe(1);
    });
});
