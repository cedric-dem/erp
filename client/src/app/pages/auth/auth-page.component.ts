import { Component, computed, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { APP_STRINGS } from '../../app.strings';

type AuthMode = 'login' | 'register';
type ProjectAction = 'join' | 'create';

interface LoginPayload {
  username: string;
  password: string;
}

interface RegisterPayload extends LoginPayload {
  project: string;
  projectAction: ProjectAction;
}

@Component({
  selector: 'app-auth-page',
  templateUrl: './auth-page.component.html',
  standalone: true,
  styleUrl: './auth-page.component.css',
  imports: [FormsModule],
})
export class AuthPageComponent {
  protected readonly title = signal('ERP Platform');
  protected readonly mode = signal<AuthMode>('login');
  protected readonly message = signal('');
  protected readonly isError = signal(false);
  protected readonly isSubmitting = signal(false);
  protected readonly strings = APP_STRINGS;

  protected readonly submitLabel = computed(() =>
    this.mode() === 'login' ? this.strings.loginSubmit : this.strings.registerSubmit,
  );

  protected loginUsername = '';
  protected loginPassword = '';
  protected registerUsername = '';
  protected registerPassword = '';
  protected registerProject = '';
  protected registerProjectAction: ProjectAction = 'join';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  protected setMode(mode: AuthMode): void {
    this.mode.set(mode);
    this.clearFeedback();
  }

  protected setProjectAction(action: ProjectAction): void {
    this.registerProjectAction = action;
    this.clearFeedback();
  }

  protected login(): void {
    if (this.isSubmitting()) {
      return;
    }

    const payload: LoginPayload = {
      username: this.loginUsername.trim(),
      password: this.loginPassword.trim(),
    };

    if (!payload.username || !payload.password) {
      this.setFeedback(this.strings.errors.loginEmpty, true);
      return;
    }

    this.isSubmitting.set(true);
    this.http.post<{ message: string }>('/api/auth/login', payload).subscribe({
      next: () => {
        this.loginPassword = '';
        sessionStorage.setItem('erpUsername', payload.username);
        this.isSubmitting.set(false);
        void this.router.navigate(['/homepage'], { state: { username: payload.username } });
      },
      error: () => {
        this.isSubmitting.set(false);
        this.setFeedback(this.strings.errors.loginInvalid, true);
      },
    });
  }

  protected register(): void {
    if (this.isSubmitting()) {
      return;
    }

    const payload: RegisterPayload = {
      username: this.registerUsername.trim(),
      password: this.registerPassword.trim(),
      project: this.registerProject.trim(),
      projectAction: this.registerProjectAction,
    };

    if (!payload.username || !payload.password || !payload.project) {
      this.setFeedback(this.strings.errors.registerEmpty, true);
      return;
    }

    this.isSubmitting.set(true);
    this.http.post<{ message: string }>('/api/auth/register', payload).subscribe({
      next: () => {
        this.setFeedback(this.strings.success.registerCreated, false);
        this.registerUsername = '';
        this.registerPassword = '';
        this.registerProject = '';
        this.registerProjectAction = 'join';
        this.mode.set('login');
        this.isSubmitting.set(false);
      },
      error: (response: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiMessage = response.error?.message;
        if (typeof apiMessage === 'string' && apiMessage.trim().length > 0) {
          this.setFeedback(apiMessage, true);
          return;
        }
        this.setFeedback(this.strings.errors.registerExists, true);
      },
    });
  }

  private clearFeedback(): void {
    this.setFeedback('', false);
  }

  private setFeedback(text: string, error: boolean): void {
    this.message.set(text);
    this.isError.set(error);
  }
}
