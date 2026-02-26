import { Component } from '@angular/core';
import { AppNotification } from '../../models/auth.models';
import { EmployerService } from '../../services/employer.service';

@Component({
  selector: 'app-employer-notifications',
  templateUrl: './employer-notifications.component.html'
})
export class EmployerNotificationsComponent {
  notifications: AppNotification[] = [];
  message = '';

  constructor(private readonly employerService: EmployerService) {
    this.load();
  }

  private load(): void {
    this.employerService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load notifications';
      }
    });
  }
}
