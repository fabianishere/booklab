import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {Book, BookItem} from "../../dataTypes";

@Component({
    selector: 'app-recommendations-list',
    templateUrl: './recommendations-list.component.html',
    styleUrls: ['./recommendations-list.component.less']
})
export class RecommendationsListComponent implements OnInit, AfterViewChecked {

    @Input() recommendations: BookItem[];
    public scrolled: boolean;

    constructor() {
        this.recommendations = [];
        this.scrolled = false;
    }

    ngOnInit() {
    }

    ngAfterViewChecked() {
        const element = document.getElementById('recommendations-list');
        if (element && !this.scrolled) {
            window.scrollTo(0, element.offsetTop);
            this.scrolled = true;
        }
    }

}
