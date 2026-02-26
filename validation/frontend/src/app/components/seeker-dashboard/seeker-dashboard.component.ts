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
  appliedJobIds = new Set<number>();
  applyingJobIds = new Set<number>();

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
    this.loadAppliedJobs();
    this.loadJobs();
  }

  get username(): string {
    return this.authService.getCurrentUser()?.username ?? 'Job Seeker';
  }

  search(): void {
    this.message = '';
    this.loadJobs();
  }

  toggleFavorite(job: JobSummary): void {
    this.favoritesService.toggle(job);
  }

  isFavorite(jobId: number): boolean {
    return this.favoritesService.isFavorite(jobId);
  }

  isApplied(jobId: number): boolean {
    return this.appliedJobIds.has(jobId);
  }

  apply(job: JobSummary): void {
    if (this.isApplied(job.id) || this.applyingJobIds.has(job.id)) {
      return;
    }
    this.applyingJobIds.add(job.id);
    this.jobSeekerService.applyToJob(job.id).subscribe({
      next: (response) => {
        this.appliedJobIds.add(job.id);
        this.message = response?.message ?? 'Application submitted';
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to apply for job';
        this.applyingJobIds.delete(job.id);
      },
      complete: () => {
        this.applyingJobIds.delete(job.id);
      }
    });
  }

  private loadJobs(): void {
    const filters = this.toSearchFilters();
    this.jobSeekerService.searchJobs(filters).subscribe({
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

  private loadAppliedJobs(): void {
    this.jobSeekerService.getAppliedJobIds().subscribe({
      next: (jobIds) => {
        this.appliedJobIds = new Set(jobIds ?? []);
      },
      error: () => {
        this.appliedJobIds = new Set<number>();
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

  private toSearchFilters(): {
    title?: string | null;
    location?: string | null;
    company?: string | null;
    jobType?: string | null;
    maxExperienceYears?: number | null;
    minSalary?: number | null;
    maxSalary?: number | null;
  } {
    const filters = this.form.getRawValue();
    const jobType = filters.jobType && filters.jobType !== 'Any job type' ? filters.jobType : null;
    const maxExperience = filters.maxExperienceYears ? Number(filters.maxExperienceYears) : null;
    const minSalary = filters.minSalary ? Number(filters.minSalary) : null;
    const maxSalary = filters.maxSalary ? Number(filters.maxSalary) : null;

    return {
      title: filters.role?.trim() || null,
      location: filters.location?.trim() || null,
      company: filters.company?.trim() || null,
      jobType,
      maxExperienceYears: Number.isFinite(maxExperience) ? maxExperience : null,
      minSalary: Number.isFinite(minSalary) ? minSalary : null,
      maxSalary: Number.isFinite(maxSalary) ? maxSalary : null
    };
  }
}
