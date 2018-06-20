import {BookSearchComponent} from './book-search.component';
import {HttpService} from "../../services/http/http.service";
import {UserService} from '../../services/user/user.service';
import {Observable} from "rxjs/Rx";
import {Book, BookItem} from "../../dataTypes";
import {ImageSearchComponent} from "../image-search/image-search.component";
import {async, TestBed} from "@angular/core/testing";
import {BooklistComponent} from "../booklist/booklist.component";
import {FormsModule} from "@angular/forms";

class MockEvent {
    public srcElement = new MockSourceElement();
}

class MockSourceElement {
    public files = [new Blob()];
}

describe('BookSearchComponent should..', () => {
    let component: BookSearchComponent;
    let http: jasmine.SpyObj<HttpService>;
    let user: jasmine.SpyObj<UserService>;


    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [ FormsModule ],
            declarations: [ImageSearchComponent,
                BooklistComponent,
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        http = jasmine.createSpyObj('HttpService', ['putImg', 'checkHealth']);
        user = jasmine.createSpyObj('UserService', ['getBookshelf', 'addMultToBookshelf']);
        component = new BookSearchComponent(user);
        component.booklist = new BooklistComponent();
        component.image = new ImageSearchComponent(http);
    });

    it('create', () => {
        expect(component).toBeTruthy();
    });

    it('send a picture to the httpservice', () => {
        http.putImg.and.returnValue(new Observable());
        component.onSubmit(new MockEvent());
        expect(http.putImg.calls.count()).toBe(1);
    });

    // it('add found books to the bookshelf', () => {
    //     let books = [new Book([new Title('test', 'MAIN')], ['auth'], ['123']),
    //         new Book([new Title('test2', 'MAIN')], ['auth2'], ['1223'])];
    //     component.results = books.map(b => new BookItem(b));
    //     component.addTo(books);
    //     expect(user.addMultToBookshelf.calls.count()).toBe(1);
    //     expect(user.addMultToBookshelf.calls.mostRecent().args[0]).toEqual(books);
    // });

});
