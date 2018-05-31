import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManualBookFindComponent } from './manual-book-find.component';

describe('ManualBookFindComponent', () => {
  let component: ManualBookFindComponent;
  let fixture: ComponentFixture<ManualBookFindComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManualBookFindComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManualBookFindComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
