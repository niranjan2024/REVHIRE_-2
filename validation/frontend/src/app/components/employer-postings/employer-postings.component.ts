import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';

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
    companyName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.minLength(50), Validators.maxLength(5000)]],
    skills: ['', [Validators.required, this.skillCountValidator]],
    maxExperienceYears: [null as number | null, [Validators.required, Validators.min(0), Validators.max(30)]],
    education: [''],
    location: ['', [Validators.required, Validators.maxLength(120)]],
    minSalary: [null as number | null, [Validators.required, Validators.min(0)]],
    maxSalary: [null as number | null, [Validators.required, Validators.min(0)]],
    jobType: ['', Validators.required],
    applicationDeadline: [''],
    openings: [null as number | null, [Validators.required, Validators.min(1), Validators.max(500)]]
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

  private skillCountValidator(control: AbstractControl): ValidationErrors | null {
    const value = (control.value ?? '').toString();
    const count = value
      .split(/[,\\s]+/)
      .map((entry: string) => entry.trim())
      .filter((entry: string) => entry.length > 0)
      .length;
    return count >= 3 ? null : { skillCount: true };
  }
}
