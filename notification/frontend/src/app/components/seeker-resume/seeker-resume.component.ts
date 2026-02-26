import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { JobSeekerService } from '../../services/job-seeker.service';

@Component({
  selector: 'app-seeker-resume',
  templateUrl: './seeker-resume.component.html'
})
export class SeekerResumeComponent implements OnInit {
  message = '';
  fileMessage = '';
  selectedFileName = 'No file chosen';

  form = this.fb.group({
    objective: ['', Validators.maxLength(500)],
    education: ['', Validators.maxLength(2000)],
    experience: ['', Validators.maxLength(2000)],
    skills: ['', Validators.maxLength(2000)],
    projects: ['', Validators.maxLength(2000)],
    certifications: ['', Validators.maxLength(2000)]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly jobSeekerService: JobSeekerService
  ) {}

  ngOnInit(): void {
    this.jobSeekerService.getResume().subscribe({
      next: (resume) => {
        this.form.patchValue({
          objective: resume.objective ?? '',
          education: resume.education ?? '',
          experience: resume.experience ?? '',
          skills: resume.skills ?? '',
          projects: resume.projects ?? '',
          certifications: resume.certifications ?? ''
        });
        if (resume.uploadedFileName) {
          this.selectedFileName = `${resume.uploadedFileName} (${this.toMbLabel(resume.uploadedFileSize)})`;
        }
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to load resume';
      }
    });
  }

  saveTextResume(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.jobSeekerService.updateResume({
      objective: raw.objective ?? '',
      education: raw.education ?? '',
      experience: raw.experience ?? '',
      skills: raw.skills ?? '',
      projects: raw.projects ?? '',
      certifications: raw.certifications ?? ''
    }).subscribe({
      next: () => {
        this.message = 'Text resume saved successfully';
      },
      error: (err) => {
        this.message = err?.error?.message ?? 'Unable to save resume';
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length ? input.files[0] : null;
    if (!file) {
      this.fileMessage = 'Please choose a file';
      return;
    }

    const allowedTypes = new Set([
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ]);
    const allowedExtensions = ['.pdf', '.doc', '.docx'];
    const lowerName = file.name.toLowerCase();
    const extensionAllowed = allowedExtensions.some((ext) => lowerName.endsWith(ext));
    if (!extensionAllowed || !allowedTypes.has(file.type)) {
      this.fileMessage = 'Only pdf, doc, or docx files are allowed';
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.fileMessage = 'File size must be up to 2MB';
      return;
    }

    this.jobSeekerService.uploadResumeFile(file).subscribe({
      next: (res) => {
        this.selectedFileName = `${res.fileName} (${this.toMbLabel(res.fileSize)})`;
        this.fileMessage = res.message;
      },
      error: (err) => {
        this.fileMessage = err?.error?.message ?? 'Resume upload failed';
      }
    });
  }

  private toMbLabel(size: number | null): string {
    if (!size && size !== 0) {
      return '';
    }
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  }
}
