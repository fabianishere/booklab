import { Component, OnInit } from '@angular/core';
import { HttpServiceService } from '../httpService/http-service.service';


interface Book {
    isbn: string;
    title: string;
}

interface DetectionResult {
    results: Book[];
}



@Component({
    selector: 'app-image',
    templateUrl: './image.component.html',
    styleUrls: ['./image.component.less']
})

export class ImageComponent implements OnInit {
    public img: any;
    public results: Book[];

    constructor(private http: HttpServiceService) {
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
        });

    }

    ngOnInit() {
        this.img = null;
        this.results = [];
    }
}
