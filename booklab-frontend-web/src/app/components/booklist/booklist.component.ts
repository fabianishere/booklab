import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {isDefined} from "@angular/compiler/src/util";
import {AddTo} from "../../interfaces/addTo";
import {BookItem, Box} from "../../interfaces/user";

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
      this.books = [];

  }

  isAdded(): boolean {
      return isDefined(this.books.find(b => b.added));
  }

    /**
     * Changes the background color of the book list entry according to mouseenter and mouseleave events.
     * @param {string} id the id of the book entry to change the background of
     * @param {boolean} enter indicates whether the event is a mouseenter event
     */
  changeColor(id: string, enter: boolean) {
      const element = document.getElementById(id);
      enter ? element.style.backgroundColor = "rgba(0, 255, 0, 1)" : element.style.backgroundColor = "rgba(255, 255, 255, 1)";
  }

}
