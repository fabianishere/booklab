import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';


import { AppComponent } from './app.component';
import { ImageComponent } from './image/image.component';
import { HeaderComponent } from './header/header.component';
import { BookshelfComponent } from "./bookshelf/bookshelf.component";

const routes: Routes = [
    {
        path: '',
        component: ImageComponent
    },
    {
        path: 'bookshelf',
        component: BookshelfComponent
    }
]

@NgModule({
    declarations: [
        AppComponent,
        ImageComponent,
        HeaderComponent,
        BookshelfComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        RouterModule.forRoot(
            routes,
            { enableTracing: true } // <-- debugging purposes only
        )
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
