import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-side-nav',
  templateUrl: './side-nav.component.html',
  standalone: true,
  styleUrl: './side-nav.component.css',
  imports: [RouterLink, RouterLinkActive]
})
export class SideNavComponent {}
