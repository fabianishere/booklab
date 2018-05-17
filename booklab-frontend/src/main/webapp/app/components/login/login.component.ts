import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {

   @Input() public app;
  constructor() { }

  ngOnInit() {
  }

  goBack() {
    this.app.login = false;
  }

}
