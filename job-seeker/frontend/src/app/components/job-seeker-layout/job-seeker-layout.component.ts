import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-job-seeker-layout',
  templateUrl: './job-seeker-layout.component.html'
})
export class JobSeekerLayoutComponent {
  showMenu = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  get usernameInitial(): string {
    const username = this.authService.getCurrentUser()?.username ?? '';
    return username.length ? username.charAt(0).toUpperCase() : 'U';
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.clearCurrentUser();
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.clearCurrentUser();
        this.router.navigate(['/login']);
      }
    });
  }
}
