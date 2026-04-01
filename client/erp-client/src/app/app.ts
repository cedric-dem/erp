import { DOCUMENT } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  imports: [RouterOutlet]
})
export class App {
  private readonly document = inject(DOCUMENT);
  private readonly storageKey = 'theme-preference';

  readonly theme = signal<'light' | 'dark'>(this.getInitialTheme());
  readonly themeToggleLabel = computed(() => (this.theme() === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'));

  constructor() {
    this.applyTheme(this.theme());
  }

  toggleTheme(): void {
    const nextTheme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(nextTheme);
    this.applyTheme(nextTheme);
  }

  private getInitialTheme(): 'light' | 'dark' {
    const persistedTheme = localStorage.getItem(this.storageKey);
    if (persistedTheme === 'light' || persistedTheme === 'dark') {
      return persistedTheme;
    }

    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private applyTheme(theme: 'light' | 'dark'): void {
    this.document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(this.storageKey, theme);
  }
}
