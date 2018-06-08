import {Component, Input, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {LoginService} from "../../services/login/login.service";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.less']
})

/**
 * Class for header component.
 */
export class HeaderComponent implements OnInit {
    @Input() public app;

    /**
     * Constructor for HeaderComponent.
     */
    constructor(public user: UserService, public login: LoginService) {}

    ngOnInit() {
    }

}
