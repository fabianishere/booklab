import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../services/user/user.service";
import {Book} from "../../interfaces/user";

@Component({
  selector: 'app-delete-popup',
  templateUrl: './delete-popup.component.html',
  styleUrls: ['./delete-popup.component.less']
})
export class DeletePopupComponent implements OnInit {

    private book: Book;

  constructor(public activePop: NgbActiveModal, private user: UserService) { }

  ngOnInit() {
  }

  deleteBook() {
      this.user.deleteFromBookshelf(this.book);
      this.activePop.dismiss();
  }

  setBook(book: Book) {
      this.book = book;
  }

}
