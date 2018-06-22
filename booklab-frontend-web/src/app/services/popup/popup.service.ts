import { Injectable } from '@angular/core';
import {LoginComponent} from "../../components/login/login.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {BookloadComponent} from "../../components/bookload-popup/bookload-popup.component";
import {AddedPopupComponent} from "../../components/added-popup/added-popup.component";
import {DeletePopupComponent} from "../../components/delete-popup/delete-popup.component";
import {Book} from "../../interfaces/user";

@Injectable({
  providedIn: 'root'
})
export class PopupService {

    private loaderRef;

  constructor(public modal: NgbModal) {
  }

  login() {
      this.modal.open(LoginComponent);
  }

  bookLoader() {
      this.loaderRef = this.modal.open(BookloadComponent,
          {backdrop: 'static', keyboard: false, centered: true});
  }

  dismissBookloader() {
      this.loaderRef.dismiss();
  }

  openIsAdded() {
      this.modal.open(AddedPopupComponent,
      {centered: true});
  }

  openDoYouWantToDelete(book: Book) {
      const deletePopup: DeletePopupComponent = this.modal.open(DeletePopupComponent,
          {backdrop: 'static', keyboard: false, centered: true}).componentInstance;
        deletePopup.setBook(book);
  }
}
