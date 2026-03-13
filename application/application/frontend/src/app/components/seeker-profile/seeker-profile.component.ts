import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { JobSeekerService } from '../../services/job-seeker.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-seeker-profile',
  templateUrl: './seeker-profile.component.html'
})
export class SeekerProfileComponent implements OnInit {
  message = '';

  form = this.fb.group({
    username: [{ value: '', disabled: true }],
    fullName: [{ value: '', disabled: true }],
    email: [{ value: '', disabled: true }],
    mobileNumber: [{ value: '', disabled: true }],
    location: [{ value: '', disabled: true }],
    skills: ['', Validators.maxLength(1000)],
    education: ['', Validators.maxLength(1000)],
    certifications: ['', Validators.maxLength(1000)],
    headline: ['', Validators.maxLength(150)],
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
          headline: profile.headline ?? '',
          summary: profile.summary ?? ''
        });
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
      skills: raw.skills ?? '',
      education: raw.education ?? '',
      certifications: raw.certifications ?? '',
      headline: raw.headline ?? '',
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
}
