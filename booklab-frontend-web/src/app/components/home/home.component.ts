import { Component, OnInit } from '@angular/core';
import {PopupService} from "../../services/popup/popup.service";
import {OAuthService} from "angular-oauth2-oidc";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.less']
})
export class HomeComponent implements OnInit {

  constructor(public login: PopupService, public oauth: OAuthService) { }

  ngOnInit() {
  }

}
