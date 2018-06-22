import {BookshelfComponent} from './bookshelf.component';
import {UserService} from "../../services/user/user.service";

describe('BookshelfComponent should..', () => {
    let component: BookshelfComponent;
    let user: jasmine.SpyObj<UserService>;


    beforeEach(() => {
        user = jasmine.createSpyObj('UserService', ['deleteFromBookshelf','getBookshelf', 'addToBookshelf']);
        component = new BookshelfComponent(user);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('delete a book', () => {
        const book = { id: '123', title: 'test', authors: ['auth'], identifiers: { internal: '123' }, categories: [], images: {} };
        component.deleteBook(book);
        expect(user.deleteFromBookshelf.calls.count()).toBe(1);
        expect(user.deleteFromBookshelf.calls.mostRecent().args[0]).toBe(book);
    });

});
