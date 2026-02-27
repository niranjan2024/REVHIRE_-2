import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';

import { JobSeekerService } from '../../services/job-seeker.service';
import { AuthService } from '../../services/auth.service';
import { EmploymentStatus } from '../../models/auth.models';

@Component({
  selector: 'app-seeker-profile',
  templateUrl: './seeker-profile.component.html'
})
export class SeekerProfileComponent implements OnInit {
  message = '';
  private employmentStatus: EmploymentStatus = 'FRESHER';

  form = this.fb.group({
    username: [{ value: '', disabled: true }],
    fullName: [{ value: '', disabled: true }],
    email: [{ value: '', disabled: true }],
    mobileNumber: [{ value: '', disabled: true }],
    location: ['', [Validators.required, Validators.maxLength(100)]],
    skills: ['', [Validators.required, Validators.maxLength(1000), this.skillCountValidator]],
    education: ['', Validators.maxLength(1000)],
    certifications: ['', Validators.maxLength(1000)],
    summary: ['', Validators.maxLength(500)]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly jobSeekerService: JobSeekerService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.form.patchValue({
        username: currentUser.username ?? '',
        email: currentUser.email ?? '',
        fullName: currentUser.fullName ?? '',
        mobileNumber: currentUser.mobileNumber ?? '',
        location: currentUser.location ?? ''
      });
      this.employmentStatus = (currentUser.employmentStatus ?? 'FRESHER') as EmploymentStatus;
    }

    this.jobSeekerService.getProfile().subscribe({
      next: (profile) => {
        this.form.patchValue({
          username: profile.username ?? currentUser?.username ?? '',
          fullName: profile.fullName ?? currentUser?.fullName ?? '',
          email: profile.email ?? currentUser?.email ?? '',
          mobileNumber: profile.mobileNumber ?? currentUser?.mobileNumber ?? '',
          location: profile.location ?? currentUser?.location ?? '',
          skills: profile.skills ?? '',
          education: profile.education ?? '',
          certifications: profile.certifications ?? '',
          summary: profile.summary ?? ''
        });
        this.employmentStatus = (profile.employmentStatus ?? currentUser?.employmentStatus ?? 'FRESHER') as EmploymentStatus;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load profile';
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.jobSeekerService.updateProfile({
      location: raw.location ?? '',
      employmentStatus: this.employmentStatus,
      skills: raw.skills ?? '',
      education: raw.education ?? '',
      certifications: raw.certifications ?? '',
      summary: raw.summary ?? ''
    }).subscribe({
      next: () => {
        this.message = 'Profile saved successfully';
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to save profile';
      }
    });
  }

  private skillCountValidator(control: AbstractControl): ValidationErrors | null {
    const value = (control.value ?? '').toString();
    const count = value
      .split(/[,\\s]+/)
      .map((entry: string) => entry.trim())
      .filter((entry: string) => entry.length > 0)
      .length;
    return count > 0 && count <= 50 ? null : { skillCount: true };
  }
}
