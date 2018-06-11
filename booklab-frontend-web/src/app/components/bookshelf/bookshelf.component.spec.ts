import {BookshelfComponent} from './bookshelf.component';
import {UserService} from "../../services/user/user.service";
import {Book, Title} from "../../dataTypes";

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
        const book: Book = new Book([new Title('test', 'MAIN')], ['auth'], ['123']);
        component.deleteBook(book);
        expect(user.deleteFromBookshelf.calls.count()).toBe(1);
        expect(user.deleteFromBookshelf.calls.mostRecent().args[0]).toBe(book);
    });

});
