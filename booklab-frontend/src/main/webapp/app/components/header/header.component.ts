import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.less']
})
export class HeaderComponent implements OnInit {
    //public login: boolean = false;
    @Input() public app;

    constructor() {}

    ngOnInit() {}
}
