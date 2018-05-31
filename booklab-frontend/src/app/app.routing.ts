
import {RouterModule, Routes} from "@angular/router";
import {ImageUploadComponent} from "./components/image-upload/image-upload.component";
import {BookshelfComponent} from "./components/bookshelf/bookshelf.component";
import {AboutComponent} from "./components/about/about.component";
import {HomeComponent} from "./components/home/home.component";

const routes: Routes = [
    {
        path: '',
        component: ImageUploadComponent
    },
    {
        path: 'bookshelf',
        component: BookshelfComponent
    },
    {
        path: 'about',
        component: AboutComponent
    }
    ,
    {
        path: 'home',
        component: HomeComponent
    }
];

export const AppRoutes = RouterModule.forRoot(routes, { enableTracing: true });
