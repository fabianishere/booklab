import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BookItem, Box} from "../../dataTypes";
import {isDefined} from "@angular/compiler/src/util";
import {AddTo} from "../../interfaces";

@Component({
  selector: 'app-booklist',
  templateUrl: './booklist.component.html',
  styleUrls: ['./booklist.component.less']
})
export class BooklistComponent implements OnInit {

    @Input() buttonText: string;
    @Input() addTo: AddTo;
    @Input() books: BookItem[];

    @Output() enterMouseEvent = new EventEmitter<Box>();
    @Output() leaveMouseEvent = new EventEmitter<Box>();

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

  changeColor(id: string, enter: boolean) {
      const element = document.getElementById(id);
      enter ? element.style.backgroundColor = "rgba(0, 255, 0, 1)" : element.style.backgroundColor = "rgba(255, 255, 255, 1)";
  }

}
