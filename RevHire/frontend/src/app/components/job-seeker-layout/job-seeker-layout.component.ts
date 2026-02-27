import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { NotificationCenterService } from '../../services/notification-center.service';

@Component({
  selector: 'app-job-seeker-layout',
  templateUrl: './job-seeker-layout.component.html'
})
export class JobSeekerLayoutComponent {
  showMenu = false;
  unreadCount = 0;

  constructor(
    private readonly authService: AuthService,
    private readonly notificationCenter: NotificationCenterService,
    private readonly router: Router
  ) {
    this.loadUnreadCount();
  }

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

  private loadUnreadCount(): void {
    const userId = this.authService.getCurrentUser()?.userId;
    if (!userId) {
      this.unreadCount = 0;
      return;
    }
    this.notificationCenter.getNotificationsForUser(userId).subscribe({
      next: (notifications) => {
        this.unreadCount = this.notificationCenter.getUnreadCount(userId, notifications ?? []);
      },
      error: () => {
        this.unreadCount = 0;
      }
    });
  }
}
