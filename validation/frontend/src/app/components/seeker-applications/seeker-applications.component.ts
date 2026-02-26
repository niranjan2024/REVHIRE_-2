import { Component } from '@angular/core';

import { JobSeekerApplication } from '../../models/auth.models';
import { JobSeekerService } from '../../services/job-seeker.service';

@Component({
  selector: 'app-seeker-applications',
  templateUrl: './seeker-applications.component.html'
})
export class SeekerApplicationsComponent {
  applications: JobSeekerApplication[] = [];
  message = '';

  constructor(private readonly jobSeekerService: JobSeekerService) {
    this.load();
  }

  withdraw(applicationId: number): void {
    const confirmed = window.confirm('Are you sure you want to withdraw this application?');
    if (!confirmed) {
      return;
    }
    const reason = window.prompt('Reason for withdrawal (optional):') ?? '';
    this.jobSeekerService.withdrawApplicationById(applicationId, reason.trim()).subscribe({
      next: (response) => {
        this.message = response?.message ?? 'Application withdrawn';
        this.load();
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to withdraw application';
      }
    });
  }

  private load(): void {
    this.jobSeekerService.getApplications().subscribe({
      next: (applications) => {
        this.applications = applications ?? [];
      },
      error: (err) => {
        this.applications = [];
        this.message = err?.error?.message ?? 'Unable to load applications';
      }
    });
  }
}
