import {Component, Input, OnInit} from '@angular/core';
import {UserService} from '../../services/user/user.service';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.less']
})
export class HeaderComponent implements OnInit {
    @Input() public app;

    constructor(public user: UserService) {
    }

    ngOnInit() {
    }
}
