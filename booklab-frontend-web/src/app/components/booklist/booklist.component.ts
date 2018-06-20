import {Component, Input, OnInit} from '@angular/core';
import {BookItem} from "../../interfaces/user";
import {isDefined} from "@angular/compiler/src/util";
import {AddTo} from "../../interfaces/addTo";

@Component({
  selector: 'app-booklist',
  templateUrl: './booklist.component.html',
  styleUrls: ['./booklist.component.less']
})
export class BooklistComponent implements OnInit {

    @Input() buttonText: string;
    @Input() addTo: AddTo;
    @Input() books: BookItem[];


  constructor() {
      this.books = [];
  }

  ngOnInit() {
  }

  add() {
      this.addTo.addTo(this.books.filter(b => b.checked).map(b => {
          b.checked = false;
          b.added = true;
          return b.book
      }));
  }

  isAdded(): boolean {
      return isDefined(this.books.find(b => b.added));
  }
}
