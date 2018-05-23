import {Component, OnInit} from '@angular/core';
import {HttpService} from '../../services/http/http.service';
import {UserService} from '../../services/user/user.service';
import {Book, DetectionResult, Title} from '../../dataTypes';

@Component({
    selector: 'app-image',
    templateUrl: './image-upload.component.html',
    styleUrls: ['./image-upload.component.less']
})

export class ImageUploadComponent implements OnInit {
    public img: any;
    public results: Book[];
    public addedToShelf: boolean;

    constructor(private http: HttpService, private user: UserService) {
    }

    ngOnInit() {
        this.img = null;
        this.results = [];
        this.addedToShelf = false;
    }

    onSubmit(event) {
        console.log('click!');
        const files = event.srcElement.files;
        const reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = () => {
            this.img = reader.result;
            this.http.checkHealth();
            this.http.putImg(ImageUploadComponent.toBlob(this.img)).subscribe((res) => {
                this.results = res.results.map(b => Book.getBook(b));
            });
        };
        this.addedToShelf = false;

    }

    addToBookShelf(event: Event) {
        console.log('click!');
        this.user.addMultToBookshelf(this.results);
        this.addedToShelf = true;
    }

    /**
     * Helper method to convert the given data URI to a blob.
     *
     * @param uri The data uri to convert.
     * @returns {Blob} The resulting binary blob.
     */
    private static toBlob(uri) {
        // convert base64 to raw binary data held in a string
        const byteString = atob(uri.split(',')[1]);

        // separate out the mime component
        const mimeString = uri.split(',')[0].split(':')[1].split(';')[0];

        // write the bytes of the string to an ArrayBuffer
        const arrayBuffer = new ArrayBuffer(byteString.length);
        const _ia = new Uint8Array(arrayBuffer);
        for (let i = 0; i < byteString.length; i++) {
            _ia[i] = byteString.charCodeAt(i);
        }

        const dataView = new DataView(arrayBuffer);
        return new Blob([dataView], { type: mimeString });
    }
}
