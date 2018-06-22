import {BookshelfComponent} from './bookshelf.component';
import {UserService} from "../../services/user/user.service";
import {PopupService} from "../../services/popup/popup.service";

describe('BookshelfComponent should..', () => {
    let component: BookshelfComponent;
    let user: jasmine.SpyObj<UserService>;
    let popup: jasmine.SpyObj<PopupService>;


    beforeEach(() => {
        user = jasmine.createSpyObj('UserService', ['deleteFromBookshelf','getBookshelf', 'addToBookshelf']);
        popup = jasmine.createSpyObj('PopupService', ['openDoYouWantToDelete']);
        component = new BookshelfComponent(user, popup);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('delete a book', () => {
        const book = { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} };
        component.deleteBook(book);
        expect(popup.openDoYouWantToDelete.calls.count()).toBe(1);
        expect(popup.openDoYouWantToDelete.calls.mostRecent().args[0]).toBe(book);
    });

});
