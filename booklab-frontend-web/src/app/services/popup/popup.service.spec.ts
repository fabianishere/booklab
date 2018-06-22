import {inject} from '@angular/core/testing';

import {PopupService} from './popup.service';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

describe('PopupService', () => {
    let login: PopupService;
    let modal: jasmine.SpyObj<NgbModal>;

  beforeEach(() => {
    modal = jasmine.createSpyObj('NgbModal', ['open']);
    login = new PopupService(modal);
  });

  it('should be created', () => {
    expect(login).toBeTruthy();
  });
});
