import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { EmployerService } from '../../services/employer.service';

@Component({
  selector: 'app-employer-company-profile',
  templateUrl: './employer-company-profile.component.html'
})
export class EmployerCompanyProfileComponent {
  message = '';

  form = this.fb.group({
    companyName: [''],
    industry: [''],
    companySize: [''],
    companyDescription: [''],
    website: [''],
    companyLocation: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly employerService: EmployerService
  ) {
    this.load();
  }

  save(): void {
    this.employerService.updateCompanyProfile(this.form.getRawValue() as {
      companyName: string;
      industry: string;
      companySize: string;
      companyDescription: string;
      website: string;
      companyLocation: string;
    }).subscribe({
      next: () => {
        this.message = 'Company profile updated';
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to update company profile';
      }
    });
  }

  private load(): void {
    this.employerService.getCompanyProfile().subscribe({
      next: (profile) => {
        this.form.patchValue({
          companyName: profile.companyName ?? '',
          industry: profile.industry ?? '',
          companySize: profile.companySize ?? '',
          companyDescription: profile.companyDescription ?? '',
          website: profile.website ?? '',
          companyLocation: profile.companyLocation ?? ''
        });
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load company profile';
      }
    });
  }
}
