import {Component, Input, OnInit} from '@angular/core';
import {UserService} from "../../services/user/user.service";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {

    @Input() public app;

    constructor(private user: UserService) {
    }

    ngOnInit() {
    }

    goBack() {
        this.app.login = false;
    }

    login(username: string, password: string) {
        this.user.login(username, password);
        this.goBack();
    }

}
