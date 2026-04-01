import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SideNavComponent } from '../../components/side-nav/side-nav.component';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
  imports: [RouterLink, SideNavComponent]
})
export class HomePageComponent {
  private readonly usernameFromState = window.history.state?.username as string | undefined;
  protected readonly username = signal(this.usernameFromState ?? sessionStorage.getItem('erpUsername') ?? 'User');

  protected logout(): void {
    sessionStorage.removeItem('erpUsername');
  }
}
