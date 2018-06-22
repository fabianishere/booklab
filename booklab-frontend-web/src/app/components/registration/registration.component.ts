import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http/http.service";
import {LoginService} from "../../services/login/login.service";
import {UserRegistration} from "../../interfaces/user";
import {PopupService} from "../../services/popup/popup.service";

@Component({
    selector: 'app-registration',
    templateUrl: './registration.component.html',
    styleUrls: ['./registration.component.less']
})
export class RegistrationComponent implements OnInit {

    public succeeded = false;
    public submitted = false;
    invalid = false;
    error = {};

    constructor(private http: HttpService, public login: PopupService) {
    }

    ngOnInit() {
    }

    /**
     * Process the given user registration request.
     *
     * @param {UserRegistration} value The user to register.
     * @param {boolean} valid A flag to indicate the request is valid.
     */
    register({value, valid}: { value: UserRegistration, valid: boolean }) {
        this.submitted = true;
        this.invalid = false;
        this.http.register(value.email, value.password).subscribe(
            res => {
                this.succeeded = true;
            },
            error =>  {
                this.invalid = true;
                this.succeeded = false;
                this.submitted = false;
                this.error = error;
            });
    }
}
