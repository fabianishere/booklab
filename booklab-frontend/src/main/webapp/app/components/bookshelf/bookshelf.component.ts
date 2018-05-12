import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import {Book} from '../../dataTypes';

@Component({
    selector: 'app-bookshelf',
    templateUrl: './bookshelf.component.html',
    styleUrls: ['./bookshelf.component.less']
})
export class BookshelfComponent implements OnInit {

    public books: Book[];

    constructor(private user: UserService) {}

    ngOnInit() {
       this.user.getBookshelf().subscribe(b => {
           this.books = b;
       });
    }

    delete(book: Book) {
        this.user.deleteFromBookshelf(book);
    }
}
