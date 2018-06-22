import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeletePopupComponent } from './delete-popup.component';

describe('DeletePopupComponent', () => {
  let component: DeletePopupComponent;

  beforeEach(() => {
    component = new DeletePopupComponent(null, null);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
