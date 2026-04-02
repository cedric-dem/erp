import { Component } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-history-page',
  templateUrl: './history-page.component.html',
  standalone: true,
  styleUrl: './history-page.component.css',
  imports: [SideNavComponent]
})
export class HistoryPageComponent {}
