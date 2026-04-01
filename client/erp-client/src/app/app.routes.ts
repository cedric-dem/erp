import { Routes } from '@angular/router';
import { AuthPageComponent } from './pages/auth/auth-page.component';
import { HomePageComponent } from './pages/home/home-page.component';

export const routes: Routes = [
  { path: '', component: AuthPageComponent },
  { path: 'homepage', component: HomePageComponent },
  { path: '**', redirectTo: '' }
];
