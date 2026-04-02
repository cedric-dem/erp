import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-users-page',
  templateUrl: './users-page.component.html',
  standalone: true,
  styleUrl: './users-page.component.css',
  imports: [SideNavComponent]
})
export class UsersPageComponent {}
