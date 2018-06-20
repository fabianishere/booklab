import {RouterModule, Routes} from "@angular/router";
import {BookSearchComponent} from "./components/book-search/book-search.component";
import {BookshelfComponent} from "./components/bookshelf/bookshelf.component";
import {AboutComponent} from "./components/about/about.component";
import {HomeComponent} from "./components/home/home.component";
import {RecommendationsComponent} from "./components/recommendations/recommendations.component";
import {SorryComponent} from "./components/sorry/sorry.component";
import {RegistrationComponent} from "./components/registration/registration.component";
import {UserService} from "./services/user/user.service";

const routes: Routes = [
    {
        path: 'register',
        component: RegistrationComponent
    },
    {
        path: '',
        component: HomeComponent
    },
    {
        path: 'sorry',
        component: SorryComponent
    },
    {
        path: 'about',
        component: AboutComponent
    },
    {
        path: '',
        canActivate: [UserService],
        children: [
            {
                path: 'upload',
                component: BookSearchComponent
            },
            {
                path: 'bookshelf',
                component: BookshelfComponent
            },
            {
                path: 'recommendations',
                component: RecommendationsComponent
            }
        ]
    }

];

export const AppRoutes = RouterModule.forRoot(routes, {enableTracing: true});
