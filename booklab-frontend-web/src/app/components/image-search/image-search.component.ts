import {Component, Input, OnInit} from '@angular/core';
import {Book, BookDetection, BookItem} from "../../dataTypes";
import {Subject} from "rxjs/Rx";
import {HttpService} from "../../services/http/http.service";
import {Observable} from "rxjs/Observable";

@Component({
    selector: 'app-image-search',
    templateUrl: './image-search.component.html',
    styleUrls: ['./image-search.component.less']
})
export class ImageSearchComponent implements OnInit {

    public img = null;
    public searching = false;



    constructor(private http: HttpService) {
    }

    ngOnInit() {
    }

    /**
     * Invoked by the image upload button, sends the image from the event to the backend for processing.
     * @param img
     */
    submit(img: File): Observable<BookItem[]> {
        const subject = new Subject<BookItem[]>();
        const reader = new FileReader();
        reader.readAsDataURL(img);
        reader.onload = () => {
            this.img = reader.result;
        };
        this.http.checkHealth();
        this.searching = true;
        this.http.putImg(img).subscribe((res) => {
            this.searching = false;
            const books = res
                .filter(b => b.matches.length > 0)
                .map((b: BookDetection) => new BookItem(b.matches[0]));
            subject.next(books);
        }, error => {
            this.searching = false;
            this.http.handleError(error)
        });
        return subject;
    }
}
