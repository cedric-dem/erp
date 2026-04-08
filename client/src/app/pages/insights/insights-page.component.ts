import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-insights-page',
  templateUrl: './insights-page.component.html',
  standalone: true,
  styleUrl: './insights-page.component.css',
  imports: [SideNavComponent],
})
export class InsightsPageComponent {}
