import { Component, inject } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings-page.component.html',
  standalone: true,
  styleUrl: './settings-page.component.css',
  imports: [SideNavComponent],
})
export class SettingsPageComponent {
  readonly themeService = inject(ThemeService);
}
