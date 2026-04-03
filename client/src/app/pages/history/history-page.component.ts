import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

interface InventoryModificationRow {
  modifiedAt: string;
  quantityMove: number;
  itemName: string;
  userName: string;
}

@Component({
  selector: 'app-history-page',
  templateUrl: './history-page.component.html',
  standalone: true,
  styleUrl: './history-page.component.css',
  imports: [SideNavComponent]
})
export class HistoryPageComponent implements OnInit {
  protected readonly modificationRows = signal<InventoryModificationRow[]>([]);
  protected readonly errorMessage = signal('');

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadHistoryModifications();
  }

  protected quantityMoveLabel(value: number): string {
    return value > 0 ? `+${value}` : `${value}`;
  }

  private loadHistoryModifications(): void {
    this.http.get<InventoryModificationRow[]>('/api/history-modifications').subscribe({
      next: (rows) => {
        this.modificationRows.set(rows);
      },
      error: () => {
        this.errorMessage.set('Could not load modification data.');
      }
    });
  }
}
