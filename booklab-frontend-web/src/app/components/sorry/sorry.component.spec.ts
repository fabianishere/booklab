import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SorryComponent } from './sorry.component';

describe('SorryComponent', () => {
  let component: SorryComponent;
  let fixture: ComponentFixture<SorryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SorryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SorryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
