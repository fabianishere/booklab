import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationsComponent } from './recommendations.component';
import {ImageSearchComponent} from "../image-search/image-search.component";
import {BooklistComponent} from "../booklist/booklist.component";
import {FormsModule} from "@angular/forms";
import {HttpService} from "../../services/http/http.service";

describe('RecommendationsComponent', () => {
  let component: RecommendationsComponent;
  let fixture: ComponentFixture<RecommendationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
        imports: [FormsModule],
        declarations: [
            RecommendationsComponent,
            ImageSearchComponent,
            BooklistComponent
        ],
        providers: [{ provide: HttpService, useValue: {} }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RecommendationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
