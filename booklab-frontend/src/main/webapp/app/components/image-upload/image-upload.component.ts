import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http/http.service';
import { UserService } from "../../services/user/user.service";
import {Book} from "../../dataTypes";

interface DetectionResult {
    results: Book[];
}



@Component({
    selector: 'app-image',
    templateUrl: './image-upload.component.html',
    styleUrls: ['./image-upload.component.less']
})

export class ImageUploadComponent implements OnInit {
    public img: any;
    public results: Book[];

    constructor(private http: HttpService, private user: UserService) {
    }

    onSubmit(event) {
        console.log('click!');
        const files = event.srcElement.files;
        const reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = () => this.img = reader.result;
        this.http.checkHealth();
        this.http.putImg(null).subscribe((res: DetectionResult) => {
            res.results.forEach(book => console.log(book.title + ' ' + book.isbn));
            this.results = res.results;
            this.user.setBookshelf(res.results);
        });

    }

    ngOnInit() {
        this.img = null;
        this.results = [];
    }
}
