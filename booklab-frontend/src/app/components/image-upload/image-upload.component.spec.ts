import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ImageUploadComponent} from './image-upload.component';
import {HttpService} from "../../services/http/http.service";
import {UserService} from '../../services/user/user.service';

describe('ImageUploadComponent', () => {
    let component: ImageUploadComponent;
    let fixture: ComponentFixture<ImageUploadComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ImageUploadComponent],
            providers: [{provide: UserService, usevalue: jasmine.createSpyObj('UserService', ['getBookshelf'])},
                {provide: HttpService, usevalue: jasmine.createSpyObj('HttpService', ['findBook'])}]

        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ImageUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
