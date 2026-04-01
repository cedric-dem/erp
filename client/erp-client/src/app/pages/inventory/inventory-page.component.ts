import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-inventory-page',
  templateUrl: './inventory-page.component.html',
  standalone: true,
  styleUrl: './inventory-page.component.css',
  imports: [SideNavComponent]
})
export class InventoryPageComponent {}
