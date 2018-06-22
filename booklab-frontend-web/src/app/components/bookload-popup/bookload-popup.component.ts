import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-popup',
  templateUrl: './bookload-popup.component.html',
  styleUrls: ['./bookload-popup.component.less']
})
export class BookloadComponent implements OnInit {


  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit() {
  }



}
