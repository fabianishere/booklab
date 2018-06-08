import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationComponent} from './registration.component';
import {HttpService} from "../../services/http/http.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";

describe('RegistrationComponent', () => {
    let component: RegistrationComponent;
    let http: jasmine.SpyObj<HttpService>;
    let fixture: ComponentFixture<RegistrationComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [RegistrationComponent],
            imports: [NgbModule.forRoot()],
            providers: [ { provide: HttpService, useValue: http }]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RegistrationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
