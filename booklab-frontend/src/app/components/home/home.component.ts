import { Component, OnInit } from '@angular/core';
import {LoginService} from "../../services/login/login.service";
import {OAuthService} from "angular-oauth2-oidc";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.less']
})
export class HomeComponent implements OnInit {

  constructor(public login: LoginService, public oauth: OAuthService) { }

  ngOnInit() {
  }

}
