import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'app-image',
    templateUrl: './image.component.html',
    styleUrls: ['./image.component.less']
})
export class ImageComponent implements OnInit {
    public img: any;

    constructor() {
        this.img = null;
    }

    onSubmit(event) {
        console.log('click!');
        const files = event.srcElement.files;
        const reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = () => this.img = reader.result;
    }

    ngOnInit() {}
}
