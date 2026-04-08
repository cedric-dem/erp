import { HttpClient } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

type UserSummary = {
  id: number;
  username: string;
  userType: string;
};

type UserSummaryApiResponse = {
  id: number;
  username: string;
  userType?: string | null;
  user_type?: string | null;
};

type UpdateUserResponse = {
  id: number;
  userType?: string | null;
  user_type?: string | null;
};

@Component({
  selector: 'app-users-page',
  templateUrl: './users-page.component.html',
  standalone: true,
  styleUrl: './users-page.component.css',
  imports: [SideNavComponent],
})
export class UsersPageComponent implements OnInit {
  protected readonly userRows = signal<UserSummary[]>([]);
  protected readonly isAdmin = signal(false);
  protected readonly promotingUserId = signal<number | null>(null);
  private readonly username = sessionStorage.getItem('erpUsername') ?? '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  protected makeUserNormal(userId: number): void {
    if (!this.isAdmin() || this.promotingUserId() !== null) {
      return;
    }

    this.promotingUserId.set(userId);
    this.http
      .patch<UpdateUserResponse>(
        `/api/users/${userId}/make-normal?username=${encodeURIComponent(this.username)}`,
        {},
      )
      .subscribe({
        next: (updatedUser) => {
          const updatedUserType = this.normalizeUserType(updatedUser.userType ?? updatedUser.user_type);
          this.userRows.update((rows) =>
            rows.map((row) =>
              row.id === updatedUser.id
                ? { ...row, userType: updatedUserType }
                : row,
            ),
          );
          this.promotingUserId.set(null);
        },
        error: () => this.promotingUserId.set(null),
      });
  }

  private loadUsers(): void {
    this.http
      .get<UserSummaryApiResponse[]>(`/api/users?username=${encodeURIComponent(this.username)}`)
      .subscribe({
        next: (rows) => {
          const mappedRows = rows.map((row) => ({
            id: row.id,
            username: row.username,
            userType: this.normalizeUserType(row.userType ?? row.user_type),
          }));

          this.userRows.set(mappedRows);
          this.isAdmin.set(
            mappedRows.some((row) => row.username === this.username && row.userType === 'admin'),
          );
        },
        error: () => {
          this.userRows.set([]);
          this.isAdmin.set(false);
        },
      });
  }

  private normalizeUserType(userType?: string | null): string {
    return (userType ?? 'normal').toLowerCase();
  }
}
