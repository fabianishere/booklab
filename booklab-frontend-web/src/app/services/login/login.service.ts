import { Injectable } from '@angular/core';
import {LoginComponent} from "../../components/login/login.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(public modal: NgbModal) {
  }

  login() {
      this.modal.open(LoginComponent);
  }
}
