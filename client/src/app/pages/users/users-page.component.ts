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
  protected readonly canManageNewUsers = signal(false);
  protected readonly promotingUserId = signal<number | null>(null);
  private readonly username = sessionStorage.getItem('erpUsername') ?? '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  protected makeUserNormal(userId: number): void {
    if (!this.canManageNewUsers() || this.promotingUserId() !== null) {
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
          this.refreshManagePermission(this.userRows());
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
          this.refreshManagePermission(mappedRows);
        },
        error: () => {
          this.userRows.set([]);
          this.canManageNewUsers.set(false);
        },
      });
  }

  private normalizeUserType(userType?: string | null): string {
    return (userType ?? 'normal').toLowerCase();
  }

  private refreshManagePermission(rows: UserSummary[]): void {
    const loggedInUser = rows.find((row) => row.username === this.username);
    this.canManageNewUsers.set(
      loggedInUser !== undefined && loggedInUser.userType !== 'new_user',
    );
  }
}
