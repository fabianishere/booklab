import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddedPopupComponent } from './added-popup.component';

describe('AddedPopupComponent', () => {
  let component: AddedPopupComponent;
  let fixture: ComponentFixture<AddedPopupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddedPopupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddedPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
