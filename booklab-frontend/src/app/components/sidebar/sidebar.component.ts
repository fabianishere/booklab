import {Component, Input, OnInit} from '@angular/core';
import {AppComponent} from "../../app.component";

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
    constructor() {
    }

    ngOnInit() {
    }

    toggle() {
        if(this.app) this.app.menuCollapse = !this.app.menuCollapse;
    }

}
