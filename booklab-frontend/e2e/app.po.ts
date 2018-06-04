import { browser, by, element } from 'protractor';

export class AppPage {
  navigateTo() {
    return browser.get('/');
  }

  getHeader() {
    return element(by.css('app-root app-header')).isPresent();
  }
}
