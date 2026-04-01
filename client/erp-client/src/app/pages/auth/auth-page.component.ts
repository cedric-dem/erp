import { Component, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { APP_STRINGS } from '../../app.strings';

@Component({
  selector: 'app-auth-page',
  templateUrl: './auth-page.component.html',
  styleUrl: './auth-page.component.css',
  imports: [FormsModule]
})
export class AuthPageComponent {
  protected readonly title = signal('ERP Platform');
  protected readonly mode = signal<'login' | 'register'>('login');
  protected readonly message = signal('');
  protected readonly isError = signal(false);
  protected readonly strings = APP_STRINGS;

  protected loginUsername = '';
  protected loginPassword = '';
  protected registerUsername = '';
  protected registerPassword = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  protected setMode(mode: 'login' | 'register'): void {
    this.mode.set(mode);
    this.message.set('');
    this.isError.set(false);
  }

  protected login(): void {
    const username = this.loginUsername.trim();
    const password = this.loginPassword.trim();

    if (!username || !password) {
      this.setFeedback(this.strings.errors.loginEmpty, true);
      return;
    }

    this.http
      .post<{ message: string }>('/api/auth/login', { username, password })
      .subscribe({
        next: () => {
          this.loginPassword = '';
          sessionStorage.setItem('erpUsername', username);
          void this.router.navigate(['/homepage'], { state: { username } });
        },
        error: () => {
          this.setFeedback(this.strings.errors.loginInvalid, true);
        }
      });
  }

  protected register(): void {
    const username = this.registerUsername.trim();
    const password = this.registerPassword.trim();

    if (!username || !password) {
      this.setFeedback(this.strings.errors.registerEmpty, true);
      return;
    }

    this.http
      .post<{ message: string }>('/api/auth/register', { username, password })
      .subscribe({
        next: () => {
          this.setFeedback(this.strings.success.registerCreated, false);
          this.registerUsername = '';
          this.registerPassword = '';
          this.mode.set('login');
        },
        error: () => {
          this.setFeedback(this.strings.errors.registerExists, true);
        }
      });
  }

  private setFeedback(text: string, error: boolean): void {
    this.message.set(text);
    this.isError.set(error);
  }
}
