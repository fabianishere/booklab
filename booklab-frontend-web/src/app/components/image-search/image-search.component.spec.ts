import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImageSearchComponent } from './image-search.component';
import {HttpService} from "../../services/http/http.service";
import {BookSearchComponent} from "../image-upload/book-search.component";

describe('ImageSearchComponent', () => {
    let component: ImageSearchComponent;
    let http: jasmine.SpyObj<HttpService>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ ImageSearchComponent ]
        })
            .compileComponents();
    }));


    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['putImg', 'checkHealth']);
        component = new ImageSearchComponent(http);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
