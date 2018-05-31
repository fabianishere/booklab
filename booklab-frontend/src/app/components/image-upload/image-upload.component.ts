import {Component, OnInit} from '@angular/core';
import {HttpService} from '../../services/http/http.service';
import {UserService} from '../../services/user/user.service';
import {Book, Title} from '../../dataTypes';

@Component({
    selector: 'app-image',
    templateUrl: './image-upload.component.html',
    styleUrls: ['./image-upload.component.less']
})

/**
 * Class for the image upload component, handles the uploading of an image and can add it to the bookshelf.
 */
export class ImageUploadComponent implements OnInit {
    public img: any;
    public results: Book[];
    public addedToShelf: boolean;
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
        this.addedToShelf = false;
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
            this.results = res.results.map(b => Book.getBook(b));
        }, error => {
            this.searching = false;
            this.http.handleError(error)
        });
        this.addedToShelf = false;

    }

    /**
     * Adds the books found in the picture to the bookshelf.
     */
    addToBookShelf() {
        console.log('click!');
        this.user.addMultToBookshelf(this.results);
        this.addedToShelf = true;
    }

    /**
     * Deletes a book from the list of books to be added to the bookshelf.
     * @param {Book} book: book to be deleted
     */
    deleteBook(book: Book) {
        this.results = this.addedToShelf? this.results :
            this.results.filter(b => b.getMainTitle()!=book.getMainTitle());
    }
}
