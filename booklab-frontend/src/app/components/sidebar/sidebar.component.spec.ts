import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarComponent } from './sidebar.component';
import {AppComponent} from "../../app.component";

class MockApp {
    menuCollapse: boolean = false;
}

describe('SidebarComponent should..', () => {
  let component: SidebarComponent;
  let app;

  beforeEach(() => {
    component = new SidebarComponent(jasmine.createSpyObj('OAuthService', ['hasValidAccessToken']));
  });

  it('create', () => {
    expect(component).toBeTruthy();
  });

  it('toggle', () => {
      app = new MockApp();
      component.app = app;
      component.toggle();
      expect(app.menuCollapse).toBeTruthy();
  });


});
