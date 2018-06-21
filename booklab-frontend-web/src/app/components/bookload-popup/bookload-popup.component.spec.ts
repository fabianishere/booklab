import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookloadComponent } from './bookload-popup.component';

describe('BookloadComponent', () => {
  let component: BookloadComponent;
  let fixture: ComponentFixture<BookloadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BookloadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BookloadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
