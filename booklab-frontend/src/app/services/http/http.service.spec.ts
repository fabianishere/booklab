import {TestBed, inject} from '@angular/core/testing';

import {HttpService} from './http.service';
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";

describe('HttpServiceService', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [HttpService,
                {provide: Router, usevalue: jasmine.createSpy('Router')},
                {provide: HttpClient, usevalue: jasmine.createSpy('HttpClient')}]
        });
    });

    it('should be created', inject([HttpService], (service: HttpService) => {
        expect(service).toBeTruthy();
    }));
});
