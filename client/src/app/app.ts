import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './services/theme.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: true,
  styleUrl: './app.css',
  imports: [RouterOutlet],
})
export class App {
  private readonly themeService = inject(ThemeService);

  constructor() {
    this.themeService.theme();
  }
}
