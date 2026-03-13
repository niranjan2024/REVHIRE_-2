import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { FavoritesService } from '../../services/favorites.service';
import { JobSummary } from '../../models/auth.models';

@Component({
  selector: 'app-seeker-dashboard',
  templateUrl: './seeker-dashboard.component.html'
})
export class SeekerDashboardComponent {
  readonly jobs: JobSummary[] = [
    { id: 1, title: 'Angular Developer', company: 'TechNova', location: 'Bengaluru', type: 'Full-time', minSalary: 500000, maxSalary: 900000, maxExperienceYears: 3 },
    { id: 2, title: 'Java Backend Engineer', company: 'BrightHire', location: 'Hyderabad', type: 'Full-time', minSalary: 700000, maxSalary: 1200000, maxExperienceYears: 5 },
    { id: 3, title: 'QA Analyst', company: 'QualityNest', location: 'Remote', type: 'Contract', minSalary: 400000, maxSalary: 650000, maxExperienceYears: 2 }
  ];

  filteredJobs: JobSummary[] = [...this.jobs];

  form = this.fb.group({
    role: [''],
    location: [''],
    maxExperienceYears: [''],
    company: [''],
    minSalary: [''],
    maxSalary: [''],
    jobType: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly favoritesService: FavoritesService
  ) {}

  get username(): string {
    return this.authService.getCurrentUser()?.username ?? 'Job Seeker';
  }

  search(): void {
    const filters = this.form.getRawValue();
    this.filteredJobs = this.jobs.filter((job) => {
      const roleOk = !filters.role || job.title.toLowerCase().includes(filters.role.toLowerCase());
      const locationOk = !filters.location || job.location.toLowerCase().includes(filters.location.toLowerCase());
      const expOk = !filters.maxExperienceYears || job.maxExperienceYears <= Number(filters.maxExperienceYears);
      const companyOk = !filters.company || job.company.toLowerCase().includes(filters.company.toLowerCase());
      const minSalaryOk = !filters.minSalary || job.maxSalary >= Number(filters.minSalary);
      const maxSalaryOk = !filters.maxSalary || job.minSalary <= Number(filters.maxSalary);
      const typeOk = !filters.jobType || filters.jobType === 'Any job type' || job.type === filters.jobType;
      return roleOk && locationOk && expOk && companyOk && minSalaryOk && maxSalaryOk && typeOk;
    });
  }

  toggleFavorite(job: JobSummary): void {
    this.favoritesService.toggle(job);
  }

  isFavorite(jobId: number): boolean {
    return this.favoritesService.isFavorite(jobId);
  }
}
