import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-added-popup',
  templateUrl: './added-popup.component.html',
  styleUrls: ['./added-popup.component.less']
})
export class AddedPopupComponent implements OnInit {

  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit() {
  }

}
