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
  markingReadIds = new Set<number>();

  constructor(
    private readonly authService: AuthService,
    private readonly notificationCenter: NotificationCenterService
  ) {
    this.load();
  }

  isRead(item: AppNotification): boolean {
    return !!item.read;
  }

  markAsRead(item: AppNotification): void {
    if (!item || item.read || this.markingReadIds.has(item.id)) {
      return;
    }
    this.markingReadIds.add(item.id);
    this.notificationCenter.markAsRead(item.id).subscribe({
      next: (updated) => {
        item.read = updated.read;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to update notification';
      },
      complete: () => {
        this.markingReadIds.delete(item.id);
      }
    });
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
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load notifications';
      }
    });
  }
}
