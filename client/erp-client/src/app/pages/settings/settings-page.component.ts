import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  standalone: true,
  styleUrl: './settings-page.component.css',
  imports: [SideNavComponent]
})
export class SettingsPageComponent {}
