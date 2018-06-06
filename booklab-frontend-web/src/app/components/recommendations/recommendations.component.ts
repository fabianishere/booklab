import {Component, OnInit, ViewChild} from '@angular/core';
import {Book, BookItem} from "../../dataTypes";
import {ImageSearchComponent} from "../image-search/image-search.component";
import {BooklistComponent} from "../booklist/booklist.component";
import {Subject} from "rxjs/Rx";
import {AddTo} from "../../interfaces";

@Component({
    selector: 'app-recommendations',
    templateUrl: './recommendations.component.html',
    styleUrls: ['./recommendations.component.less']
})
export class RecommendationsComponent implements OnInit, AddTo {

    @ViewChild(ImageSearchComponent) image: ImageSearchComponent;
    @ViewChild(BooklistComponent) booklist: BooklistComponent;

    constructor() {
    }

    ngOnInit() {
    }

    addTo(books: Book[]) {
        console.log('Stuff works!');
    }

    onSubmit(event) {
        this.booklist.books = [];
        this.image
            .submit(event.srcElement.files[0])
            .subscribe(res => {
                this.booklist.books = res;
            });
    }
}
