import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationsComponent } from './recommendations.component';
import {ImageSearchComponent} from "../image-search/image-search.component";
import {BooklistComponent} from "../booklist/booklist.component";
import {FormsModule} from "@angular/forms";
import {HttpService} from "../../services/http/http.service";
import {UserService} from "../../services/user/user.service";
import {RecommendationsListComponent} from "../recommendations-list/recommendations-list.component";

describe('RecommendationsComponent', () => {
    let component: RecommendationsComponent;
    let http: jasmine.SpyObj<HttpService>;
    let user: jasmine.SpyObj<UserService>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [ FormsModule ],
            declarations: [
                RecommendationsComponent,
                ImageSearchComponent,
                BooklistComponent,
                RecommendationsListComponent,
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['putImg', 'checkHealth']);
        user = jasmine.createSpyObj('UserService', ['getBookshelf', 'addMultToBookshelf']);
        component = new RecommendationsComponent(http, user);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
