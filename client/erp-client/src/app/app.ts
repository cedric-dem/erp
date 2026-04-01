import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';

interface HealthResponse {
  status: string;
  service: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);

  protected readonly title = signal('ERP Platform');
  protected readonly serverStatus = signal('Checking server connection...');

  ngOnInit(): void {
    this.http.get<HealthResponse>('/api/health').subscribe({
      next: (response) => {
        this.serverStatus.set(`Connected (${response.service}: ${response.status})`);
      },
      error: () => {
        this.serverStatus.set('Server not reachable. Start backend on port 8080 and configure proxy/CORS.');
      }
    });
  }
}
