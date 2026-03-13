import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';

import { AuthService } from '../../services/auth.service';
import { FavoritesService } from '../../services/favorites.service';
import { EmployerJob, JobSummary } from '../../models/auth.models';
import { JobSeekerService } from '../../services/job-seeker.service';

@Component({
  selector: 'app-seeker-dashboard',
  templateUrl: './seeker-dashboard.component.html'
})
export class SeekerDashboardComponent {
  jobs: JobSummary[] = [];

  filteredJobs: JobSummary[] = [];
  message = '';

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
    private readonly favoritesService: FavoritesService,
    private readonly jobSeekerService: JobSeekerService
  ) {
    this.loadJobs();
  }

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

  private loadJobs(): void {
    this.jobSeekerService.getOpenJobs().subscribe({
      next: (jobs) => {
        this.jobs = jobs.map((job) => this.toJobSummary(job));
        this.filteredJobs = [...this.jobs];
      },
      error: (err) => {
        this.jobs = [];
        this.filteredJobs = [];
        this.message = err?.error?.message ?? 'Unable to load jobs';
      }
    });
  }

  private toJobSummary(job: EmployerJob): JobSummary {
    return {
      id: job.id,
      title: job.title,
      company: job.companyName,
      location: job.location,
      type: job.jobType,
      minSalary: Number(job.minSalary),
      maxSalary: Number(job.maxSalary),
      maxExperienceYears: job.maxExperienceYears
    };
  }
}
