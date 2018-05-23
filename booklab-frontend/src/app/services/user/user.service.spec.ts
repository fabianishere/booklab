import {TestBed, inject} from '@angular/core/testing';

import {UserService} from './user.service';
import {OAuthService} from "angular-oauth2-oidc";


describe('UserService', () => {
    let authSpy: jasmine.SpyObj<OAuthService>;
    beforeEach(() => {
        authSpy = jasmine.createSpyObj('OAuthService', ['hasValidAccessToken'])
        authSpy.hasValidAccessToken.and.returnValue(true);
    });


    it('should be created', () => {
        expect(new UserService(authSpy)).toBeTruthy();
    });
});
