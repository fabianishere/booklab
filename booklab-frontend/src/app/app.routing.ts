
import {RouterModule, Routes} from "@angular/router";
import {ImageUploadComponent} from "./components/image-upload/image-upload.component";
import {BookshelfComponent} from "./components/bookshelf/bookshelf.component";

const routes: Routes = [
    {
        path: '',
        component: ImageUploadComponent
    },
    {
        path: 'bookshelf',
        component: BookshelfComponent
    }
];

export const AppRoutes = RouterModule.forRoot(routes, { enableTracing: true });
