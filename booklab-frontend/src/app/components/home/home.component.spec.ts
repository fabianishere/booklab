import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import {LoginService} from "../../services/login/login.service";
import {OAuthService} from "angular-oauth2-oidc";

describe('HomeComponent', () => {
  let component: HomeComponent;
  let login: jasmine.SpyObj<LoginService>;
  let auth: jasmine.SpyObj<OAuthService>;


    beforeEach(() => {
      login = jasmine.createSpyObj('LoginService', ['login']);
      auth = jasmine.createSpyObj('OAuthService', ['hasValidAccessToken']);
      component = new HomeComponent(login, auth);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
