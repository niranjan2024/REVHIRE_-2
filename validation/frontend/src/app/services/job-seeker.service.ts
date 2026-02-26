import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AppNotification,
  EmployerJob,
  JobSeekerApplication,
  JobSeekerProfile,
  JobSeekerProfileRequest,
  ResumeData,
  ResumeRequest,
  ResumeUploadResponse
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class JobSeekerService {
  private readonly baseUrl = '/api';

  constructor(private readonly http: HttpClient) {}

  getProfile(): Observable<JobSeekerProfile> {
    return this.http.get<JobSeekerProfile>(`${this.baseUrl}/jobseeker/profile`, { withCredentials: true });
  }

  updateProfile(request: JobSeekerProfileRequest): Observable<JobSeekerProfile> {
    return this.http.put<JobSeekerProfile>(`${this.baseUrl}/jobseeker/profile`, request, { withCredentials: true });
  }

  getResume(): Observable<ResumeData> {
    return this.http.get<ResumeData>(`${this.baseUrl}/resume`, { withCredentials: true });
  }

  updateResume(request: ResumeRequest): Observable<ResumeData> {
    return this.http.put<ResumeData>(`${this.baseUrl}/resume`, request, { withCredentials: true });
  }

  uploadResumeFile(file: File): Observable<ResumeUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ResumeUploadResponse>(`${this.baseUrl}/resume/upload`, formData, { withCredentials: true });
  }

  searchJobs(filters: {
    title?: string | null;
    location?: string | null;
    company?: string | null;
    jobType?: string | null;
    maxExperienceYears?: number | null;
    minSalary?: number | null;
    maxSalary?: number | null;
    datePosted?: string | null;
  }): Observable<EmployerJob[]> {
    const params: Record<string, string> = {};
    if (filters.title) {
      params['title'] = filters.title;
    }
    if (filters.location) {
      params['location'] = filters.location;
    }
    if (filters.company) {
      params['company'] = filters.company;
    }
    if (filters.jobType) {
      params['jobType'] = filters.jobType;
    }
    if (filters.maxExperienceYears !== null && filters.maxExperienceYears !== undefined) {
      params['maxExperienceYears'] = String(filters.maxExperienceYears);
    }
    if (filters.minSalary !== null && filters.minSalary !== undefined) {
      params['minSalary'] = String(filters.minSalary);
    }
    if (filters.maxSalary !== null && filters.maxSalary !== undefined) {
      params['maxSalary'] = String(filters.maxSalary);
    }
    if (filters.datePosted) {
      params['datePosted'] = filters.datePosted;
    }

    return this.http.get<EmployerJob[]>(`${this.baseUrl}/jobseeker/jobs/search`, { params, withCredentials: true });
  }

  applyToJob(jobId: number, coverLetter?: string | null): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/jobseeker/jobs/${jobId}/apply`,
      { coverLetter: coverLetter ?? '' },
      { withCredentials: true }
    );
  }

  getAppliedJobIds(): Observable<number[]> {
    return this.http.get<number[]>(`${this.baseUrl}/jobseeker/jobs/applied`, { withCredentials: true });
  }

  getApplications(): Observable<JobSeekerApplication[]> {
    return this.http.get<JobSeekerApplication[]>(`${this.baseUrl}/jobseeker/jobs/applications`, { withCredentials: true });
  }

  withdrawApplication(jobId: number, reason?: string | null): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/jobseeker/jobs/${jobId}/withdraw`,
      { confirm: true, reason: reason ?? '' },
      { withCredentials: true }
    );
  }

  withdrawApplicationById(applicationId: number, reason?: string | null): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.baseUrl}/jobseeker/jobs/applications/${applicationId}/withdraw`,
      { confirm: true, reason: reason ?? '' },
      { withCredentials: true }
    );
  }

  getNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/jobseeker/notifications`, { withCredentials: true });
  }
}
