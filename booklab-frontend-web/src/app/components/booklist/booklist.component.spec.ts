import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BooklistComponent } from './booklist.component';
import {FormsModule} from "@angular/forms";

describe('BooklistComponent', () => {
    let component: BooklistComponent;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [ FormsModule ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        component = new BooklistComponent();

    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
