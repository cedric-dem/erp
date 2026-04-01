import { Routes } from '@angular/router';
import { AuthPageComponent } from './pages/auth/auth-page.component';
import { HomePageComponent } from './pages/home/home-page.component';
import { InventoryPageComponent } from './pages/inventory/inventory-page.component';
import { BillPageComponent } from './pages/bill/bill-page.component';
import { SettingsPageComponent } from './pages/settings/settings-page.component';

export const routes: Routes = [
  { path: '', component: AuthPageComponent },
  { path: 'homepage', component: HomePageComponent },
  { path: 'inventory', component: InventoryPageComponent },
  { path: 'bill', component: BillPageComponent },
  { path: 'settings', component: SettingsPageComponent },
  { path: '**', redirectTo: '' }
];
