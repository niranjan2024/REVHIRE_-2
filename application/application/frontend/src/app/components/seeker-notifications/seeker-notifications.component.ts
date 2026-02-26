import { Component } from '@angular/core';
import { AppNotification } from '../../models/auth.models';
import { JobSeekerService } from '../../services/job-seeker.service';

@Component({
  selector: 'app-seeker-notifications',
  templateUrl: './seeker-notifications.component.html'
})
export class SeekerNotificationsComponent {
  notifications: AppNotification[] = [];
  message = '';

  constructor(private readonly jobSeekerService: JobSeekerService) {
    this.load();
  }

  private load(): void {
    this.jobSeekerService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load notifications';
      }
    });
  }
}
