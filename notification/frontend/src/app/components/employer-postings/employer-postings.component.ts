import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { EmployerJob, JobStatus } from '../../models/auth.models';
import { EmployerService } from '../../services/employer.service';

@Component({
  selector: 'app-employer-postings',
  templateUrl: './employer-postings.component.html'
})
export class EmployerPostingsComponent {
  jobs: EmployerJob[] = [];
  message = '';
  editingJobId: number | null = null;

  form = this.fb.group({
    companyName: ['', Validators.required],
    title: ['', Validators.required],
    description: ['', Validators.required],
    skills: [''],
    maxExperienceYears: [null as number | null, [Validators.required, Validators.min(0)]],
    education: [''],
    location: ['', Validators.required],
    minSalary: [null as number | null, [Validators.required, Validators.min(1)]],
    maxSalary: [null as number | null, [Validators.required, Validators.min(1)]],
    jobType: ['', Validators.required],
    applicationDeadline: [''],
    openings: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly employerService: EmployerService
  ) {
    this.loadJobs();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload = {
      ...(this.form.getRawValue() as {
        companyName: string;
        title: string;
        description: string;
        skills: string;
        maxExperienceYears: number;
        education: string;
        location: string;
        minSalary: number;
        maxSalary: number;
        jobType: string;
        applicationDeadline: string;
        openings: number;
      }),
      applicationDeadline: this.form.value.applicationDeadline || null
    };

    const request$ = this.editingJobId
      ? this.employerService.updateJob(this.editingJobId, payload)
      : this.employerService.createJob(payload);

    request$.subscribe({
      next: () => {
        this.message = this.editingJobId ? 'Job updated successfully' : 'Job created successfully';
        this.resetForm();
        this.loadJobs();
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to save job';
      }
    });
  }

  edit(job: EmployerJob): void {
    this.editingJobId = job.id;
    this.form.patchValue({
      companyName: job.companyName,
      title: job.title,
      description: job.description,
      skills: job.skills ?? '',
      maxExperienceYears: job.maxExperienceYears,
      education: job.education ?? '',
      location: job.location,
      minSalary: Number(job.minSalary),
      maxSalary: Number(job.maxSalary),
      jobType: job.jobType,
      applicationDeadline: job.applicationDeadline ?? '',
      openings: job.openings
    });
  }

  remove(jobId: number): void {
    this.employerService.deleteJob(jobId).subscribe({
      next: () => {
        this.message = 'Job deleted successfully';
        this.loadJobs();
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to delete job';
      }
    });
  }

  changeStatus(job: EmployerJob, status: JobStatus): void {
    const request$ =
      status === 'OPEN'
        ? this.employerService.reopenJob(job.id)
        : status === 'CLOSED'
          ? this.employerService.closeJob(job.id)
          : this.employerService.fillJob(job.id);

    request$.subscribe({
      next: () => this.loadJobs(),
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to change job status';
      }
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  private loadJobs(): void {
    this.employerService.getJobs().subscribe({
      next: (jobs) => {
        this.jobs = jobs;
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load jobs';
      }
    });
  }

  private resetForm(): void {
    this.editingJobId = null;
    this.form.reset({
      companyName: '',
      title: '',
      description: '',
      skills: '',
      maxExperienceYears: null,
      education: '',
      location: '',
      minSalary: null,
      maxSalary: null,
      jobType: '',
      applicationDeadline: '',
      openings: null
    });
  }
}
