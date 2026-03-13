import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AppNotification,
  EmployerJob,
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

  getOpenJobs(): Observable<EmployerJob[]> {
    return this.http.get<EmployerJob[]>(`${this.baseUrl}/jobs`, { withCredentials: true });
  }

  getNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/jobseeker/notifications`, { withCredentials: true });
  }
}
