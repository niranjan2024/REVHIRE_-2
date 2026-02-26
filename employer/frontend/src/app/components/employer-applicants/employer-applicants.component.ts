import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';

import { ApplicationStatus, EmployerApplicant } from '../../models/auth.models';
import { EmployerService } from '../../services/employer.service';

@Component({
  selector: 'app-employer-applicants',
  templateUrl: './employer-applicants.component.html'
})
export class EmployerApplicantsComponent {
  applicants: EmployerApplicant[] = [];
  message = '';
  readonly statuses: ApplicationStatus[] = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'REJECTED', 'WITHDRAWN'];

  form = this.fb.group({
    status: [''],
    search: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly employerService: EmployerService
  ) {
    this.load();
  }

  filter(): void {
    this.load();
  }

  private load(): void {
    const raw = this.form.getRawValue();
    this.employerService.getApplicants((raw.status as ApplicationStatus) || '', raw.search ?? '').subscribe({
      next: (applicants) => {
        this.applicants = applicants;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load applicants';
      }
    });
  }
}
