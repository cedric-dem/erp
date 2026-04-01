import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-bill-page',
  templateUrl: './bill-page.component.html',
  standalone: true,
  styleUrl: './bill-page.component.css',
  imports: [SideNavComponent]
})
export class BillPageComponent {}
