import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookshelfComponent } from './bookshelf.component';
import {UserService} from "../../services/user/user.service";
import {FormsModule} from "@angular/forms";
import {HttpService} from "../../services/http/http.service";

describe('BookshelfComponent', () => {
    let component: BookshelfComponent;
    let fixture: ComponentFixture<BookshelfComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ BookshelfComponent ],
            imports: [FormsModule],
            providers: [{provide: UserService, usevalue: jasmine.createSpyObj('UserService', ['getBookshelf'])},
                {provide: HttpService, usevalue: jasmine.createSpyObj('HttpService', ['findBook'])}]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BookshelfComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
