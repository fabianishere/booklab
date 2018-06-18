import {Component, OnInit} from '@angular/core';
import {HttpService} from '../../services/http/http.service';
import {UserService} from '../../services/user/user.service';
import {Book, BookItem} from '../../dataTypes';
import {isDefined} from "@angular/compiler/src/util";


@Component({
    selector: 'app-image-upload',
    templateUrl: './image-upload.component.html',
    styleUrls: ['./image-upload.component.less']
})

/**
 * Class for the image upload component, handles the uploading of an image and can add it to the bookshelf.
 */
export class ImageUploadComponent implements OnInit {
    public img: any;
    public results: BookItem[];
    public enterBook = false;
    public searching = false;

    /**
     * Constructor for ImageUploadComponent.
     * @param {HttpService} http
     * @param {UserService} user
     */
    constructor(private http: HttpService, private user: UserService) {
    }

    ngOnInit() {
        this.img = null;
        this.results = [];
    }

    /**
     * Invoked by the image upload button, sends the image from the event to the backend for processing.
     * @param event: contains the image from the input element
     */
    onSubmit(event) {
        console.log('click!');
        const files = event.srcElement.files;
        const reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = () => {
            this.img = reader.result;
        };
        this.http.checkHealth();
        this.searching = true;
        this.results = [];
        this.http.putImg(files[0]).subscribe((res) => {
            this.searching = false;
            this.results = res
                .filter(b => b.matches.length > 0)
                .map(b => new BookItem(b.matches[0]));
        }, error => {
            this.searching = false;
            this.http.handleError(error)
        });
    }

    /**
     * Adds the books found in the picture to the bookshelf.
     */
    addToBookShelf() {
        console.log('click!');
        this.user.addMultToBookshelf(this.results.filter(b => b.checked).map(b => {
            b.checked = false;
            b.addedToShelf = true;
            return b.book;
        }));
    }

    /**
     * Deletes a book from the list of books to be added to the bookshelf.
     * @param {Book} book: book to be deleted
     */
    deleteBook(book: Book) {
        this.results = this.results.filter(b => b.book.title != book.title);
    }

    booksAddedToShelf(): boolean {
        return isDefined(this.results.find(b => b.addedToShelf));
    }

}
