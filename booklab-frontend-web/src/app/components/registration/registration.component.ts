import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../services/http/http.service";
import {Subject} from "rxjs/Rx";
import {LoginService} from "../../services/login/login.service";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.less']
})
export class RegistrationComponent implements OnInit {

    public succeeded = false;
    public submitted = false;

  constructor(private http: HttpService, public login: LoginService) { }

  ngOnInit() {
  }

  registrate(email: string, password: string) {
        this.submitted = true;
        this.http.register(email, password).subscribe(res => this.succeeded = true);
  }

}
