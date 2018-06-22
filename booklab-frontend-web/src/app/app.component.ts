import { Component } from '@angular/core';
import { NullValidationHandler, OAuthService } from 'angular-oauth2-oidc';

import { authConfig } from './auth.config';
import {UserService} from "./services/user/user.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'BookLab';
  login = false;
  menuCollapse = true;


  /**
   * Construct a new {@link AppComponent}.
   *
   * @param oauthService {OAuthService} The services used as OAuth client.
   */
  constructor(private oauthService: OAuthService, private user: UserService) {
      this.configureAuthorization();
      this.user.loadUser();
  }

  /**
   * Configure the OAuth authorization for the REST API we connect to.
   */
  private configureAuthorization() {
      this.oauthService.configure(authConfig);
      this.oauthService.tokenValidationHandler = new NullValidationHandler();
  }
}
