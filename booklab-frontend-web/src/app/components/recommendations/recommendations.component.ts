import {Component, OnInit, ViewChild} from '@angular/core';
import {Book, BookItem} from "../../interfaces/user";
import {ImageSearchComponent} from "../image-search/image-search.component";
import {BooklistComponent} from "../booklist/booklist.component";
import {AddTo} from "../../interfaces/addTo";
import {UserService} from "../../services/user/user.service";
import {HttpService} from "../../services/http/http.service";
import {RecommendationsListComponent} from "../recommendations-list/recommendations-list.component";

@Component({
    selector: 'app-recommendations',
    templateUrl: './recommendations.component.html',
    styleUrls: ['./recommendations.component.less']
})
export class RecommendationsComponent implements OnInit, AddTo {

    @ViewChild(ImageSearchComponent) image: ImageSearchComponent;
    @ViewChild(BooklistComponent) booklist: BooklistComponent;
    @ViewChild(RecommendationsListComponent) recommendationslist: RecommendationsListComponent;

    public books: Book[];
    public candidates: Book[];

    constructor(private http: HttpService, private user: UserService) {
    }

    ngOnInit() {
        this.candidates = [];
        this.books = [];
        this.user.getBookshelf().subscribe(b => {
            this.books = b;
        });
    }

    addTo(books: Book[]) {
        this.candidates = books;
    }

    onSubmit(event) {
        this.booklist.books = [];
        this.image
            .submit(event.srcElement.files[0])
            .subscribe(res => {
                this.booklist.books = res;
            });
    }

    recommend() {
        this.recommendationslist.recommendations = [];
        this.http.getRecommendations(this.books, this.candidates).subscribe((res) => {
            this.recommendationslist.recommendations = res.map(book => new BookItem(book))
        });
    }
}
