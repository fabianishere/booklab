import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { HttpServiceService } from "./httpService/http-service.service";


import { AppComponent } from './app.component';
import { ImageComponent } from './image/image.component';
import { HeaderComponent } from './header/header.component';
import { SidebarComponent } from './sidebar/sidebar.component';


@NgModule({
    declarations: [
        AppComponent,
        ImageComponent,
        HeaderComponent,
        SidebarComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule
    ],
    providers: [
        HttpServiceService
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
