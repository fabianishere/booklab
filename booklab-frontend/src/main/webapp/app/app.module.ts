import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { HttpService } from './services/http/http.service';
import { UserService } from "./services/user/user.service";


import { AppComponent } from './app.component';
import { ImageUploadComponent } from './components/image-upload/image-upload.component';
import { HeaderComponent } from './components/header/header.component';
import { BookshelfComponent } from "./components/bookshelf/bookshelf.component";
import { SidebarComponent} from "./components/sidebar/sidebar.component";

const routes: Routes = [
    {
        path: '',
        component: ImageUploadComponent
    },
    {
        path: 'bookshelf',
        component: BookshelfComponent
    }
]

@NgModule({
    declarations: [
        AppComponent,
        ImageUploadComponent,
        HeaderComponent,
        BookshelfComponent,
        SidebarComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        RouterModule.forRoot(
            routes,
            { enableTracing: true } // <-- debugging purposes only
        )
    ],
    providers: [
        HttpService,
        UserService
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
