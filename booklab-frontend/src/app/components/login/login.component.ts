import {Component, Input, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {Secure} from '../../dataTypes';
import {Router} from '@angular/router';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {

    @Input() public app;
    invalid = false;

    constructor(private user: UserService, private router: Router, private activeModal: NgbActiveModal){
    }

    ngOnInit() {
    }

    goBack() {
        this.activeModal.dismiss();
    }

    login(username: string, password: string) {
        if (!username || !password) {
            this.invalid = true;
            return;
        }
        username = Secure.checkInput(username);
        password = Secure.checkInput(password);
        this.invalid = false;
        this.user.login(username, password).then((resp) => {
            if (resp) {
                this.user.loggedIn = true;
                this.router.navigate(['']);
                this.goBack();
            }
        }, rejected => this.invalid = true);
    }

}
