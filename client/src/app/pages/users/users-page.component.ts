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

@Component({
  selector: 'app-users-page',
  templateUrl: './users-page.component.html',
  standalone: true,
  styleUrl: './users-page.component.css',
  imports: [SideNavComponent]
})
export class UsersPageComponent implements OnInit {
  protected readonly userRows = signal<UserSummary[]>([]);
  private readonly username = sessionStorage.getItem('erpUsername') ?? '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<UserSummaryApiResponse[]>(`/api/users?username=${encodeURIComponent(this.username)}`).subscribe({
      next: (rows) => this.userRows.set(rows.map((row) => ({
        id: row.id,
        username: row.username,
        userType: (row.userType ?? row.user_type ?? 'normal').toLowerCase()
      }))),
      error: () => this.userRows.set([])
    });
  }
}
