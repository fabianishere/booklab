import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.less']
})
export class AboutComponent implements OnInit {

    vera = 'Vera Hoveling'
    sayra = 'Sayra Ranjha'
    marino = 'Marijn Roelvink'
    fabian = 'Fabian Mastenbroek'
    christian = 'Christian Slothouber'

  constructor() { }

  ngOnInit() {
  }

}
