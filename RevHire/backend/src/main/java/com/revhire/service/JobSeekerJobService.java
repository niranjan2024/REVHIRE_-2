package com.revhire.service;

import com.revhire.dto.ApplyRequest;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.JobSeekerApplicationResponse;
import com.revhire.dto.WithdrawRequest;
import com.revhire.entity.ApplicationStatus;
import com.revhire.entity.JobApplication;
import com.revhire.entity.JobPosting;
import com.revhire.entity.JobStatus;
import com.revhire.entity.NotificationType;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.entity.Resume;
import com.revhire.repository.JobApplicationRepository;
import com.revhire.repository.JobPostingRepository;
import com.revhire.repository.ResumeRepository;
import com.revhire.repository.UserRepository;
import com.revhire.service.NotificationService;
import com.revhire.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class JobSeekerJobService {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final NotificationService notificationService;

    public JobSeekerJobService(UserRepository userRepository,
                               JobPostingRepository jobPostingRepository,
                               JobApplicationRepository jobApplicationRepository,
                               ResumeRepository resumeRepository,
                               NotificationService notificationService) {
        this.userRepository = userRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.resumeRepository = resumeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<EmployerJobResponse> searchJobs(String username,
                                                String title,
                                                String location,
                                                String company,
                                                String jobType,
                                                Integer maxExperienceYears,
                                                BigDecimal minSalary,
                                                BigDecimal maxSalary,
                                                LocalDate datePosted) {
        getJobSeeker(username);
        validateSearchFilters(title, maxExperienceYears, minSalary, maxSalary, datePosted);
        String normalizedTitle = normalize(title);
        String normalizedLocation = normalize(location);
        String normalizedCompany = normalize(company);
        String normalizedJobType = clean(jobType);

        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN).stream()
                .filter(job -> matchesPartial(job.getTitle(), normalizedTitle))
                .filter(job -> matchesPartial(job.getLocation(), normalizedLocation))
                .filter(job -> matchesPartial(job.getCompanyName(), normalizedCompany))
                .filter(job -> matchesJobType(job.getJobType(), normalizedJobType))
                .filter(job -> matchesExperience(job.getMaxExperienceYears(), maxExperienceYears))
                .filter(job -> salaryOverlaps(job.getMinSalary(), job.getMaxSalary(), minSalary, maxSalary))
                .filter(job -> matchesDatePosted(job.getCreatedAt(), datePosted))
                .map(this::toJobResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> getAppliedJobIds(String username) {
        User jobSeeker = getJobSeeker(username);
        return jobApplicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(jobSeeker.getId()).stream()
                .filter(application -> application.getStatus() != ApplicationStatus.WITHDRAWN)
                .map(application -> application.getJob().getId())
                .toList();
    }

    @Transactional
    public void applyToJob(String username, Long jobId, ApplyRequest request) {
        User jobSeeker = getJobSeeker(username);
        if (resumeRepository.findByUserId(jobSeeker.getId()).isEmpty()) {
            throw new com.revhire.exception.BadRequestException( "Resume required before applying");
        }
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "Job not found"));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new com.revhire.exception.BadRequestException( "Job must be ACTIVE before applying");
        }
        String coverLetter = request == null ? null : InputSanitizer.sanitize(request.getCoverLetter(), "coverLetter");
        JobApplication existing = jobApplicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeeker.getId());
        if (existing != null && existing.getStatus() != ApplicationStatus.WITHDRAWN) {
            throw new com.revhire.exception.ConflictException( "You have already applied for this job");
        }
        if (existing != null) {
            throw new com.revhire.exception.ConflictException( "Withdrawn applications cannot be modified");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setCoverLetter(coverLetter);
        jobApplicationRepository.save(application);
        notifyEmployerOnApply(application);
        notifySeekerOnApply(application);
    }

    @Transactional(readOnly = true)
    public List<JobSeekerApplicationResponse> getApplications(String username) {
        User jobSeeker = getJobSeeker(username);
        Resume resume = findResume(jobSeeker);
        return jobApplicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(jobSeeker.getId()).stream()
                .map(application -> toApplicationResponse(application, resume))
                .toList();
    }

    @Transactional
    public void withdrawApplication(String username, Long jobId, WithdrawRequest request) {
        User jobSeeker = getJobSeeker(username);
        JobApplication application = jobApplicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeeker.getId());
        if (application == null) {
            throw new com.revhire.exception.NotFoundException( "Application not found");
        }
        if (request == null || !Boolean.TRUE.equals(request.getConfirm())) {
            throw new com.revhire.exception.BadRequestException( "Confirmation required");
        }
        if (!application.getJobSeeker().getId().equals(jobSeeker.getId())) {
            throw new com.revhire.exception.ForbiddenException( "You can withdraw only your application");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return;
        }
        application.setWithdrawReason(InputSanitizer.sanitize(request.getReason(), "withdrawReason"));
        application.setStatus(ApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(application);
        notifyEmployerOnWithdraw(application);
    }

    @Transactional
    public void withdrawApplicationById(String username, Long applicationId, WithdrawRequest request) {
        User jobSeeker = getJobSeeker(username);
        JobApplication application = jobApplicationRepository.findByIdAndJobSeekerId(applicationId, jobSeeker.getId())
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "Application not found"));
        if (request == null || !Boolean.TRUE.equals(request.getConfirm())) {
            throw new com.revhire.exception.BadRequestException( "Confirmation required");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return;
        }
        application.setWithdrawReason(InputSanitizer.sanitize(request.getReason(), "withdrawReason"));
        application.setStatus(ApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(application);
        notifyEmployerOnWithdraw(application);
    }

    private boolean matchesPartial(String value, String normalizedFilter) {
        if (normalizedFilter == null || normalizedFilter.isBlank()) {
            return true;
        }
        return safe(value).toLowerCase(Locale.ROOT).contains(normalizedFilter);
    }

    private boolean matchesJobType(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return safe(value).equalsIgnoreCase(filter);
    }

    private boolean matchesExperience(Integer jobMaxExperience, Integer maxExperienceFilter) {
        if (maxExperienceFilter == null) {
            return true;
        }
        if (jobMaxExperience == null) {
            return false;
        }
        return jobMaxExperience <= maxExperienceFilter;
    }

    private boolean salaryOverlaps(BigDecimal jobMin, BigDecimal jobMax, BigDecimal filterMin, BigDecimal filterMax) {
        if (filterMin == null && filterMax == null) {
            return true;
        }
        if (jobMin == null || jobMax == null) {
            return false;
        }
        if (filterMin != null && jobMax.compareTo(filterMin) < 0) {
            return false;
        }
        if (filterMax != null && jobMin.compareTo(filterMax) > 0) {
            return false;
        }
        return true;
    }

    private boolean matchesDatePosted(LocalDateTime createdAt, LocalDate datePosted) {
        if (datePosted == null || createdAt == null) {
            return true;
        }
        LocalDate createdDate = createdAt.toLocalDate();
        return !createdDate.isBefore(datePosted);
    }

    private EmployerJobResponse toJobResponse(JobPosting job) {
        EmployerJobResponse response = new EmployerJobResponse();
        response.setId(job.getId());
        response.setCompanyName(job.getCompanyName());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setSkills(job.getSkills());
        response.setEducation(job.getEducation());
        response.setMaxExperienceYears(job.getMaxExperienceYears());
        response.setLocation(job.getLocation());
        response.setMinSalary(job.getMinSalary());
        response.setMaxSalary(job.getMaxSalary());
        response.setJobType(job.getJobType());
        response.setOpenings(job.getOpenings());
        response.setApplicationDeadline(job.getApplicationDeadline());
        response.setStatus(job.getStatus());
        response.setCreatedAt(job.getCreatedAt());
        return response;
    }

    private JobSeekerApplicationResponse toApplicationResponse(JobApplication application, Resume resume) {
        JobPosting job = application.getJob();
        JobSeekerApplicationResponse response = new JobSeekerApplicationResponse();
        response.setApplicationId(application.getId());
        response.setJobId(job.getId());
        response.setTitle(job.getTitle());
        response.setCompanyName(job.getCompanyName());
        response.setLocation(job.getLocation());
        response.setJobType(job.getJobType());
        response.setMinSalary(job.getMinSalary());
        response.setMaxSalary(job.getMaxSalary());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setNotes(application.getNotes());
        response.setResumeSummary(buildResumeSummary(resume));
        if (resume != null) {
            response.setResumeFileName(resume.getUploadedFileName());
            response.setResumeFileType(resume.getUploadedFileType());
            response.setResumeFileSize(resume.getUploadedFileSize());
            response.setResumeFileReference(resume.getUploadedFileReference());
        }
        return response;
    }

    private User getJobSeeker(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "User not found"));
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new com.revhire.exception.ForbiddenException( "Only job seekers can access this module");
        }
        return user;
    }

    private String normalize(String value) {
        String sanitized = InputSanitizer.sanitize(value, "filter");
        if (sanitized == null) {
            return null;
        }
        return sanitized.toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return InputSanitizer.sanitize(value, "value");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Resume findResume(User jobSeeker) {
        return resumeRepository.findByUserId(jobSeeker.getId()).orElse(null);
    }

    private String buildResumeSummary(Resume resume) {
        if (resume == null) {
            return null;
        }
        String summary = String.join(" | ",
                safe(resume.getObjective()),
                safe(resume.getSkills()),
                safe(resume.getEducationCsv()),
                safe(resume.getExperienceCsv()),
                safe(resume.getProjectsCsv()),
                safe(resume.getCertificationsCsv())
        ).trim();
        return summary.isBlank() ? null : summary;
    }

    private void notifyEmployerOnApply(JobApplication application) {
        User employer = application.getJob().getEmployer();
        String message = "New application for " + safe(application.getJob().getTitle())
                + " from " + safe(application.getJobSeeker().getUsername());
        notificationService.createNotification(employer, application.getJob(), message, NotificationType.APPLICATION_RECEIVED);
    }

    private void notifyEmployerOnWithdraw(JobApplication application) {
        User employer = application.getJob().getEmployer();
        String message = "Application withdrawn for " + safe(application.getJob().getTitle())
                + " by " + safe(application.getJobSeeker().getUsername());
        notificationService.createNotification(employer, application.getJob(), message, NotificationType.APPLICATION_UPDATE);
    }

    private void notifySeekerOnApply(JobApplication application) {
        User seeker = application.getJobSeeker();
        String message = "You applied for " + safe(application.getJob().getTitle())
                + " at " + safe(application.getJob().getCompanyName());
        notificationService.createNotification(seeker, application.getJob(), message, NotificationType.APPLICATION_UPDATE);
    }

    private void validateSearchFilters(String title,
                                       Integer maxExperienceYears,
                                       BigDecimal minSalary,
                                       BigDecimal maxSalary,
                                       LocalDate datePosted) {
        if (title != null && title.trim().length() > 100) {
            throw new com.revhire.exception.BadRequestException( "Job role must be at most 100 characters");
        }
        if (maxExperienceYears != null && (maxExperienceYears < 0 || maxExperienceYears > 30)) {
            throw new com.revhire.exception.BadRequestException( "Experience must be between 0 and 30 years");
        }
        if (minSalary != null && minSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.revhire.exception.BadRequestException( "Minimum salary must be at least 0");
        }
        if (maxSalary != null && maxSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.revhire.exception.BadRequestException( "Maximum salary must be at least 0");
        }
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new com.revhire.exception.BadRequestException( "Minimum salary cannot exceed maximum salary");
        }
        if (datePosted != null && datePosted.isAfter(LocalDate.now())) {
            throw new com.revhire.exception.BadRequestException( "Date posted cannot be in the future");
        }
    }
}
