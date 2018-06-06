import {TestBed, async} from '@angular/core/testing';
import {AppComponent} from './app.component';
import {SidebarComponent} from "./components/sidebar/sidebar.component";
import {BookshelfComponent} from "./components/bookshelf/bookshelf.component";
import {HeaderComponent} from './components/header/header.component';
import {BookSearchComponent} from "./components/image-upload/book-search.component";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {AppRoutes} from "./app.routing";
import {OAuthModule} from "angular-oauth2-oidc";
import {HttpService} from "./services/http/http.service";
import {UserService} from "./services/user/user.service";
import {APP_BASE_HREF} from "@angular/common";
import {LoginComponent} from "./components/login/login.component";
import {SorryComponent} from "./components/sorry/sorry.component";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ManualBookFindComponent} from "./components/manual-book-find/manual-book-find.component";
import {AboutComponent} from "./components/about/about.component";
import {HomeComponent} from "./components/home/home.component";
import {RecommendationsComponent} from "./components/recommendations/recommendations.component";
import {RegistrationComponent} from "./components/registration/registration.component";
import {BooklistComponent} from "./components/booklist/booklist.component";
import {ImageSearchComponent} from "./components/image-search/image-search.component";

describe('AppComponent', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                AppComponent,
                BookSearchComponent,
                HeaderComponent,
                BookshelfComponent,
                SidebarComponent,
                LoginComponent,
                SorryComponent,
                ManualBookFindComponent,
                AboutComponent,
                HomeComponent,
                RecommendationsComponent,
                RegistrationComponent,
                BooklistComponent,
                ImageSearchComponent
            ],
            imports: [
                BrowserModule,
                FormsModule,
                HttpClientModule,
                AppRoutes,
                OAuthModule.forRoot({
                    resourceServer: {
                        allowedUrls: ['http://localhost:8080'],
                        sendAccessToken: true,
                    },
                }),
                NgbModule.forRoot()
            ],
            providers: [
                HttpService,
                UserService,
                {provide: APP_BASE_HREF, useValue : '/' }
            ]
        }).compileComponents();
    }));
    it('should create the app', async(() => {
        const fixture = TestBed.createComponent(AppComponent);
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    }));
});
