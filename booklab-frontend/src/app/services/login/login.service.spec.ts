import {inject} from '@angular/core/testing';

import {LoginService} from './login.service';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

describe('LoginService', () => {
    let login: LoginService;
    let modal: jasmine.SpyObj<NgbModal>;

  beforeEach(() => {
    modal = jasmine.createSpyObj('NgbModal', ['open']);
    login = new LoginService(modal);
  });

  it('should be created', () => {
    expect(login).toBeTruthy();
  });
});
