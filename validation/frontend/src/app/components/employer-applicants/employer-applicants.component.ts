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
  editingNotesId: number | null = null;
  noteDraft = '';

  form = this.fb.group({
    status: [''],
    search: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly employerService: EmployerService
  ) {
    this.load();
    this.form.valueChanges.subscribe(() => {
      this.load();
    });
  }

  filter(): void {
    this.load();
  }

  setStatus(applicant: EmployerApplicant, status: ApplicationStatus): void {
    if (!applicant || applicant.status === status) {
      return;
    }
    const request = status === 'SHORTLISTED'
      ? this.employerService.shortlistApplicant(applicant.applicationId)
      : status === 'REJECTED'
        ? this.employerService.rejectApplicant(applicant.applicationId)
        : status === 'UNDER_REVIEW'
          ? this.employerService.setUnderReview(applicant.applicationId)
          : this.employerService.updateApplicantStatus(applicant.applicationId, status);

    request.subscribe({
      next: (updated) => {
        applicant.status = updated.status;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to update applicant status';
      }
    });
  }

  beginNotes(applicant: EmployerApplicant): void {
    this.editingNotesId = applicant.applicationId;
    this.noteDraft = applicant.notes ?? '';
  }

  cancelNotes(): void {
    this.editingNotesId = null;
    this.noteDraft = '';
  }

  saveNotes(applicant: EmployerApplicant): void {
    if (!applicant || this.editingNotesId !== applicant.applicationId) {
      return;
    }
    this.employerService.updateApplicantNotes(applicant.applicationId, this.noteDraft).subscribe({
      next: (updated) => {
        applicant.notes = updated.notes ?? this.noteDraft;
        this.cancelNotes();
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to save notes';
      }
    });
  }

  private load(): void {
    const raw = this.form.getRawValue();
    this.employerService.getApplicants((raw.status as ApplicationStatus) || '', raw.search ?? '').subscribe({
      next: (applicants) => {
        this.applicants = applicants;
        this.editingNotesId = null;
        this.noteDraft = '';
        this.message = '';
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load applicants';
      }
    });
  }
}
