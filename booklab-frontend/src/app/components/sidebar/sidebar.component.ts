import {Component, Input, OnInit} from '@angular/core';
import {AppComponent} from "../../app.component";
import {OAuthService} from "angular-oauth2-oidc";

@Component({
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.less']
})

/**
 * Class for sidebar component.
 */
export class SidebarComponent implements OnInit {

    @Input() public app: AppComponent;
    /**
     * Constructor for SidebarComponent
     */
    constructor(public oauth: OAuthService) {
    }

    ngOnInit() {
    }

    toggle() {
        if(this.app) this.app.menuCollapse = !this.app.menuCollapse;
    }

}
