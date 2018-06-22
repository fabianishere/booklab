import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {LoginComponent} from './login.component';
import {UserService} from "../../services/user/user.service";
import {Router} from "@angular/router";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

describe('LoginComponent should..', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let user: jasmine.SpyObj<UserService>;
    let router: jasmine.SpyObj<Router>;
    let modal: jasmine.SpyObj<NgbActiveModal>;


    beforeEach(() => {
        user = jasmine.createSpyObj('UserService', ['login']);
        router = jasmine.createSpyObj('Router', ['navigate']);
        modal = jasmine.createSpyObj('NgbActiveModal', ['dismiss']);
        user.login.and.returnValue(new Promise(null));
        component = new LoginComponent(user, router, modal );
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('login when given good input', () => {
        component.login('test', 'password');
        expect(user.login.calls.count()).toBe(1);
        expect(user.login.calls.mostRecent().args).toEqual(['test', 'password']);
    });

    it('be invalid when empty input is given', () => {
        component.login('', null);
        expect(component.invalid).toBeTruthy();
        expect(user.login.calls.count()).toBe(0);
    });

});
