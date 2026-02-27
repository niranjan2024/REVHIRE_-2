import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AppNotification,
  ApplicationStatus,
  EmployerApplicant,
  EmployerCompanyProfile,
  EmployerJob,
  EmployerJobRequest,
  EmployerStatistics
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class EmployerService {
  private readonly baseUrl = '/api/employer';

  constructor(private readonly http: HttpClient) {}

  getCompanyProfile(): Observable<EmployerCompanyProfile> {
    return this.http.get<EmployerCompanyProfile>(`${this.baseUrl}/company-profile`, { withCredentials: true });
  }

  updateCompanyProfile(request: EmployerCompanyProfile): Observable<EmployerCompanyProfile> {
    return this.http.put<EmployerCompanyProfile>(`${this.baseUrl}/company-profile`, request, { withCredentials: true });
  }

  getJobs(): Observable<EmployerJob[]> {
    return this.http.get<EmployerJob[]>(`${this.baseUrl}/jobs`, { withCredentials: true });
  }

  createJob(request: EmployerJobRequest): Observable<EmployerJob> {
    return this.http.post<EmployerJob>(`${this.baseUrl}/jobs`, request, { withCredentials: true });
  }

  updateJob(jobId: number, request: EmployerJobRequest): Observable<EmployerJob> {
    return this.http.put<EmployerJob>(`${this.baseUrl}/jobs/${jobId}`, request, { withCredentials: true });
  }

  deleteJob(jobId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/jobs/${jobId}`, { withCredentials: true });
  }

  closeJob(jobId: number): Observable<EmployerJob> {
    return this.http.patch<EmployerJob>(`${this.baseUrl}/jobs/${jobId}/close`, {}, { withCredentials: true });
  }

  reopenJob(jobId: number): Observable<EmployerJob> {
    return this.http.patch<EmployerJob>(`${this.baseUrl}/jobs/${jobId}/reopen`, {}, { withCredentials: true });
  }

  fillJob(jobId: number): Observable<EmployerJob> {
    return this.http.patch<EmployerJob>(`${this.baseUrl}/jobs/${jobId}/fill`, {}, { withCredentials: true });
  }

  getStatistics(): Observable<EmployerStatistics> {
    return this.http.get<EmployerStatistics>(`${this.baseUrl}/statistics`, { withCredentials: true });
  }

  getApplicants(status?: ApplicationStatus | '', search?: string): Observable<EmployerApplicant[]> {
    const params = new URLSearchParams();
    if (status) {
      params.set('status', status);
    }
    if (search && search.trim()) {
      params.set('search', search.trim());
    }
    const query = params.toString();
    const url = query ? `${this.baseUrl}/applicants?${query}` : `${this.baseUrl}/applicants`;
    return this.http.get<EmployerApplicant[]>(url, { withCredentials: true });
  }

  updateApplicantStatus(applicationId: number, status: ApplicationStatus): Observable<EmployerApplicant> {
    return this.http.patch<EmployerApplicant>(
      `${this.baseUrl}/applicants/${applicationId}/status`,
      { status },
      { withCredentials: true }
    );
  }

  shortlistApplicant(applicationId: number): Observable<EmployerApplicant> {
    return this.http.patch<EmployerApplicant>(`${this.baseUrl}/applicants/${applicationId}/shortlist`, {}, { withCredentials: true });
  }

  rejectApplicant(applicationId: number): Observable<EmployerApplicant> {
    return this.http.patch<EmployerApplicant>(`${this.baseUrl}/applicants/${applicationId}/reject`, {}, { withCredentials: true });
  }

  setUnderReview(applicationId: number): Observable<EmployerApplicant> {
    return this.http.patch<EmployerApplicant>(`${this.baseUrl}/applicants/${applicationId}/under-review`, {}, { withCredentials: true });
  }

  updateApplicantNotes(applicationId: number, notes: string): Observable<EmployerApplicant> {
    return this.http.patch<EmployerApplicant>(
      `${this.baseUrl}/applicants/${applicationId}/notes`,
      { notes },
      { withCredentials: true }
    );
  }

  getApplicantResumeFile(applicationId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/applications/${applicationId}/resume`, {
      withCredentials: true,
      responseType: 'blob'
    });
  }

  getNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>('/api/employer/notifications', { withCredentials: true });
  }
}
