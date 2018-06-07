import {RouterModule, Routes} from "@angular/router";
import {ImageUploadComponent} from "./components/image-upload/image-upload.component";
import {BookshelfComponent} from "./components/bookshelf/bookshelf.component";
import {AboutComponent} from "./components/about/about.component";
import {HomeComponent} from "./components/home/home.component";
import {RecommendationsComponent} from "./components/recommendations/recommendations.component";
import {SorryComponent} from "./components/sorry/sorry.component";

const routes: Routes = [
    {
        path: '',
        component: HomeComponent
    },
    {
        path: 'bookshelf',
        component: BookshelfComponent
    },
    {
        path: 'about',
        component: AboutComponent
    },
    {
        path: 'sorry',
        component: SorryComponent
    }
    ,
    {
        path: 'upload',
        component: ImageUploadComponent
    }
    ,
    {
        path: 'recommendations',
        component: RecommendationsComponent
    }

];

export const AppRoutes = RouterModule.forRoot(routes, {enableTracing: true});
