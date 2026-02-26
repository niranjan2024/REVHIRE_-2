import { Component } from '@angular/core';
import { AppNotification } from '../../models/auth.models';
import { AuthService } from '../../services/auth.service';
import { NotificationCenterService } from '../../services/notification-center.service';

@Component({
  selector: 'app-employer-notifications',
  templateUrl: './employer-notifications.component.html'
})
export class EmployerNotificationsComponent {
  notifications: AppNotification[] = [];
  message = '';
  readIds = new Set<number>();

  constructor(
    private readonly authService: AuthService,
    private readonly notificationCenter: NotificationCenterService
  ) {
    this.load();
  }

  isRead(notificationId: number): boolean {
    return this.readIds.has(notificationId);
  }

  private load(): void {
    const userId = this.authService.getCurrentUser()?.userId;
    if (!userId) {
      this.message = 'Unable to load notifications';
      return;
    }
    this.notificationCenter.getNotificationsForUser(userId).subscribe({
      next: (notifications) => {
        this.notifications = notifications ?? [];
        this.notificationCenter.markAllRead(userId, this.notifications);
        this.readIds = new Set(this.notificationCenter.getReadIds(userId));
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load notifications';
      }
    });
  }
}
