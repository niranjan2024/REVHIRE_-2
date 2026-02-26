import { Component } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { EmployerStatistics } from '../../models/auth.models';
import { EmployerService } from '../../services/employer.service';

@Component({
  selector: 'app-employer-dashboard',
  templateUrl: './employer-dashboard.component.html'
})
export class EmployerDashboardComponent {
  stats: EmployerStatistics = {
    totalJobs: 0,
    activeJobs: 0,
    totalApplications: 0,
    pendingReviews: 0
  };
  message = '';

  constructor(
    private readonly authService: AuthService,
    private readonly employerService: EmployerService
  ) {
    this.load();
  }

  get username(): string {
    return this.authService.getCurrentUser()?.fullName ?? this.authService.getCurrentUser()?.username ?? 'Employer';
  }

  private load(): void {
    this.employerService.getStatistics().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load dashboard statistics';
      }
    });
  }
}
