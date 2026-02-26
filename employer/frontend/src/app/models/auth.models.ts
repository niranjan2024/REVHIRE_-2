export type Role = 'JOB_SEEKER' | 'EMPLOYER';
export type JobStatus = 'OPEN' | 'CLOSED' | 'FILLED';
export type ApplicationStatus = 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'REJECTED' | 'WITHDRAWN';
export type EmploymentStatus = 'FRESHER' | 'EXPERIENCE';

export interface RegisterRequest {
  username: string;
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  mobileNumber: string;
  securityQuestion: string;
  securityAnswer: string;
  location: string;
  employmentStatus: EmploymentStatus;
  role: Role;
  companyName?: string;
  industry?: string;
  companySize?: string;
  companyDescription?: string;
  website?: string;
  companyLocation?: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface AuthResponse {
  userId: number;
  username: string;
  email: string;
  fullName: string | null;
  mobileNumber: string | null;
  location: string | null;
  role: Role;
}

export interface JobSeekerProfile {
  username: string;
  fullName: string;
  email: string;
  mobileNumber: string;
  location: string;
  skills: string | null;
  education: string | null;
  certifications: string | null;
  headline: string | null;
  summary: string | null;
}

export interface JobSeekerProfileRequest {
  skills: string;
  education: string;
  certifications: string;
  headline: string;
  summary: string;
}

export interface ResumeData {
  objective: string | null;
  education: string | null;
  experience: string | null;
  projects: string | null;
  certifications: string | null;
  skills: string | null;
  uploadedFileName: string | null;
  uploadedFileType: string | null;
  uploadedFileSize: number | null;
}

export interface ResumeRequest {
  objective: string;
  education: string;
  experience: string;
  projects: string;
  certifications: string;
  skills: string;
}

export interface ResumeUploadResponse {
  message: string;
  fileName: string;
  fileType: string;
  fileSize: number;
}

export interface JobSummary {
  id: number;
  title: string;
  company: string;
  location: string;
  type: string;
  minSalary: number;
  maxSalary: number;
  maxExperienceYears: number;
}

export interface EmployerCompanyProfile {
  companyName: string | null;
  industry: string | null;
  companySize: string | null;
  companyDescription: string | null;
  website: string | null;
  companyLocation: string | null;
}

export interface EmployerJobRequest {
  companyName: string;
  title: string;
  description: string;
  skills: string;
  education: string;
  maxExperienceYears: number;
  location: string;
  minSalary: number;
  maxSalary: number;
  jobType: string;
  openings: number;
  applicationDeadline: string | null;
}

export interface EmployerJob {
  id: number;
  companyName: string;
  title: string;
  description: string;
  skills: string | null;
  education: string | null;
  maxExperienceYears: number;
  location: string;
  minSalary: number;
  maxSalary: number;
  jobType: string;
  openings: number;
  applicationDeadline: string | null;
  status: JobStatus;
  createdAt: string;
}

export interface EmployerStatistics {
  totalJobs: number;
  activeJobs: number;
  totalApplications: number;
  pendingReviews: number;
}

export interface EmployerApplicant {
  applicationId: number;
  jobId: number;
  jobTitle: string;
  applicantUsername: string;
  applicantFullName: string;
  applicantEmail: string;
  applicantSkills: string | null;
  status: ApplicationStatus;
  appliedAt: string;
}

export interface AppNotification {
  id: number;
  jobId: number | null;
  message: string;
  read: boolean;
  createdAt: string;
}
