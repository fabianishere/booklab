import {Component, OnInit, Output} from '@angular/core';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.less']
})
export class HeaderComponent implements OnInit {
    @Output() public login: boolean = false;

    constructor() {}

    ngOnInit() {}
}
