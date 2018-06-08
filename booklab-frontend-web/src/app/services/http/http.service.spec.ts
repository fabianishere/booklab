import {TestBed, getTestBed} from '@angular/core/testing';

import {HttpService} from './http.service';
import {OAuthService, UrlHelperService} from "angular-oauth2-oidc";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../../environments/environment";
import {SorryComponent} from "../../components/sorry/sorry.component";
import {Router} from "@angular/router";

describe('HttpService should..', () => {
    let injector: TestBed;
    let client: HttpTestingController;
    let router;
    let oauth: OAuthService;
    let http: HttpService;

    beforeEach(() => {
        router = {
            navigate: jasmine.createSpy('navigate'),
        };

        TestBed.configureTestingModule({
            declarations: [SorryComponent],
            imports: [HttpClientTestingModule],
            providers: [HttpService, OAuthService, UrlHelperService, { provide: Router, useValue: router }]
        });

        injector = getTestBed();

        client = injector.get(HttpTestingController);
        oauth = injector.get(OAuthService);
        http = injector.get(HttpService);
    });

    afterEach(() => {
        client.verify();
    });

    it('be created', () => {
        expect(http).toBeTruthy();
    });

    it('post an image to the backend', () => {
        http.putImg(null).subscribe(res => {
            expect(res.length).toBe(1);
        });
        const req = client.expectOne(`${environment.apiUrl}/detection`);
        expect(req.request.method).toBe("POST");
        req.flush({
            data: [{
                box: {},
                matches: []
            }]
        });
    });

    it('send a message to the backend to find a book', () => {
        http.findBook('testname', 'testAuthor').subscribe(res => {
            expect(res.length).toBe(1);
        });
        const req = client.expectOne(req => {
            return req.url == `${environment.apiUrl}/catalogue/`
        });
        expect(req.request.method).toBe("GET");
        expect(req.request.params.get("title")).toBe("testname");
        expect(req.request.params.get("author")).toBe("testAuthor");
        req.flush({
            data: [{}]
        });

    });

    it('navigate to sorry page when called upon', () => {
        http.handleError(null);
        expect(router.navigate).toHaveBeenCalledWith(['/sorry']);
    });
});
