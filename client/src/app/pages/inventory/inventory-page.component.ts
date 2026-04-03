import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

type InventoryRow = [name: string, quantity: number, price: number];

interface InventoryItem {
  id: number;
  name: string;
  quantity: number;
  price: number;
}

@Component({
  selector: 'app-inventory-page',
  templateUrl: './inventory-page.component.html',
  standalone: true,
  styleUrl: './inventory-page.component.css',
  imports: [SideNavComponent, FormsModule]
})
export class InventoryPageComponent implements OnInit {
  protected readonly inventoryRows = signal<InventoryRow[]>([]);
  protected readonly showAddForm = signal(false);
  protected readonly errorMessage = signal('');

  protected newName = '';
  protected newQuantity = 0;
  protected newPrice = 0;

  private readonly username = sessionStorage.getItem('erpUsername') ?? 'User';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadInventory();
  }

  protected toggleAddForm(): void {
    this.showAddForm.set(!this.showAddForm());
    this.errorMessage.set('');
  }

  protected closeAddForm(): void {
    this.showAddForm.set(false);
    this.errorMessage.set('');
  }

  protected addInventoryItem(): void {
    const name = this.newName.trim();

    if (!name || this.newQuantity < 0 || this.newPrice < 0) {
      this.errorMessage.set('Enter a name, quantity, and price using non-negative values.');
      return;
    }

    this.http
      .post<InventoryItem>('/api/inventory', {
        name,
        quantity: this.newQuantity,
        price: this.newPrice,
        userName: this.username
      })
      .subscribe({
        next: (createdItem) => {
          this.inventoryRows.update((rows) => [
            ...rows,
            [createdItem.name, createdItem.quantity, createdItem.price]
          ]);
          this.newName = '';
          this.newQuantity = 0;
          this.newPrice = 0;
          this.closeAddForm();
        },
        error: () => {
          this.errorMessage.set('Could not add the item. Please try again.');
        }
      });
  }

  protected formatPrice(value: number): string {
    return value.toFixed(2);
  }

  private loadInventory(): void {
    this.http.get<InventoryItem[]>('/api/inventory').subscribe({
      next: (items) => {
        this.inventoryRows.set(items.map((item) => [item.name, item.quantity, item.price]));
      },
      error: () => {
        this.errorMessage.set('Could not load inventory data.');
      }
    });
  }
}
