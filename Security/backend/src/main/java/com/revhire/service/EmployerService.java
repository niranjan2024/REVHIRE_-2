package com.revhire.service;

import com.revhire.dto.EmployerApplicantResponse;
import com.revhire.dto.EmployerCompanyProfileRequest;
import com.revhire.dto.EmployerCompanyProfileResponse;
import com.revhire.dto.EmployerJobRequest;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.EmployerStatisticsResponse;
import com.revhire.dto.ResumeFilePayload;
import com.revhire.entity.ApplicationStatus;
import com.revhire.entity.EmployerProfile;
import com.revhire.entity.JobApplication;
import com.revhire.entity.JobPosting;
import com.revhire.entity.JobSeekerProfile;
import com.revhire.entity.JobStatus;
import com.revhire.entity.NotificationType;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.entity.Resume;
import com.revhire.repository.EmployerProfileRepository;
import com.revhire.repository.JobApplicationRepository;
import com.revhire.repository.JobPostingRepository;
import com.revhire.repository.JobSeekerProfileRepository;
import com.revhire.repository.ResumeRepository;
import com.revhire.repository.UserRepository;
import com.revhire.service.NotificationService;
import com.revhire.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EmployerService {
    private static final DateTimeFormatter JOB_POSTED_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final ResumeRepository resumeRepository;
    private final NotificationService notificationService;

    public EmployerService(UserRepository userRepository,
                           EmployerProfileRepository employerProfileRepository,
                           JobPostingRepository jobPostingRepository,
                           JobApplicationRepository jobApplicationRepository,
                           JobSeekerProfileRepository jobSeekerProfileRepository,
                           ResumeRepository resumeRepository,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.resumeRepository = resumeRepository;
        this.notificationService = notificationService;
    }

    public EmployerCompanyProfileResponse getCompanyProfile(String username) {
        User employer = getEmployer(username);
        EmployerProfile profile = employerProfileRepository.findByUser(employer)
                .orElseGet(() -> createDefaultProfile(employer));
        return toCompanyProfileResponse(profile);
    }

    public EmployerCompanyProfileResponse updateCompanyProfile(String username, EmployerCompanyProfileRequest request) {
        User employer = getEmployer(username);
        EmployerProfile profile = employerProfileRepository.findByUser(employer)
                .orElseGet(() -> createDefaultProfile(employer));

        validateCompanyProfile(request);
        profile.setCompanyName(InputSanitizer.require(request.getCompanyName(), "companyName"));
        profile.setIndustry(InputSanitizer.require(request.getIndustry(), "industry"));
        profile.setCompanySize(InputSanitizer.require(request.getCompanySize(), "companySize"));
        profile.setCompanyDescription(InputSanitizer.sanitize(request.getCompanyDescription(), "companyDescription"));
        profile.setWebsite(InputSanitizer.sanitize(request.getWebsite(), "website"));
        profile.setCompanyLocation(InputSanitizer.sanitize(request.getCompanyLocation(), "companyLocation"));
        employerProfileRepository.save(profile);
        return toCompanyProfileResponse(profile);
    }

    public List<EmployerJobResponse> getEmployerJobs(String username) {
        User employer = getEmployer(username);
        return jobPostingRepository.findByEmployerOrderByCreatedAtDesc(employer).stream()
                .map(this::toJobResponse)
                .toList();
    }

    @Transactional
    public EmployerJobResponse createJob(String username, EmployerJobRequest request) {
        validateJobRequest(request);
        User employer = getEmployer(username);

        JobPosting job = new JobPosting();
        job.setEmployer(employer);
        applyJobRequest(job, request, resolveCompanyName(employer, request.getCompanyName()));
        job.setStatus(JobStatus.OPEN);
        JobPosting saved = jobPostingRepository.save(job);

        notifyEmployerOnJobPosted(employer, saved);
        createNotificationsForMatchingSeekers(saved);
        return toJobResponse(saved);
    }

    @Transactional
    public EmployerJobResponse updateJob(String username, Long jobId, EmployerJobRequest request) {
        validateJobRequest(request);
        User employer = getEmployer(username);
        JobPosting job = getEmployerJob(jobId, employer);
        applyJobRequest(job, request, resolveCompanyName(employer, request.getCompanyName()));
        return toJobResponse(jobPostingRepository.save(job));
    }

    @Transactional
    public void deleteJob(String username, Long jobId) {
        User employer = getEmployer(username);
        JobPosting job = getEmployerJob(jobId, employer);
        jobApplicationRepository.deleteByJobId(job.getId());
        jobPostingRepository.delete(job);
    }

    @Transactional
    public EmployerJobResponse setJobStatus(String username, Long jobId, JobStatus status) {
        User employer = getEmployer(username);
        JobPosting job = getEmployerJob(jobId, employer);
        job.setStatus(status);
        return toJobResponse(jobPostingRepository.save(job));
    }

    public EmployerStatisticsResponse getStatistics(String username) {
        User employer = getEmployer(username);
        EmployerStatisticsResponse response = new EmployerStatisticsResponse();
        response.setTotalJobs(jobPostingRepository.countByEmployer(employer));
        response.setActiveJobs(jobPostingRepository.countByEmployerAndStatus(employer, JobStatus.OPEN));
        response.setTotalApplications(jobApplicationRepository.countByEmployerId(employer.getId()));
        response.setPendingReviews(jobApplicationRepository.countByEmployerIdAndStatus(employer.getId(), ApplicationStatus.UNDER_REVIEW));
        return response;
    }

    public List<EmployerApplicantResponse> getApplicants(String username, ApplicationStatus status, String search) {
        User employer = getEmployer(username);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return jobApplicationRepository.findByEmployerId(employer.getId()).stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> filterBySearch(app, normalizedSearch))
                .map(app -> toApplicantResponse(app, findProfileSkills(app.getJobSeeker()), findResume(app.getJobSeeker())))
                .toList();
    }

    public List<EmployerApplicantResponse> getApplicantsForJob(String username, Long jobId) {
        User employer = getEmployer(username);
        return jobApplicationRepository.findByEmployerIdAndJobId(employer.getId(), jobId).stream()
                .map(app -> toApplicantResponse(app, findProfileSkills(app.getJobSeeker()), findResume(app.getJobSeeker())))
                .toList();
    }

    @Transactional
    public EmployerApplicantResponse updateApplicantStatus(String username, Long applicationId, ApplicationStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        User employer = getEmployer(username);
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (!application.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your job applications");
        }
        if (application.getStatus() == status) {
            return toApplicantResponse(application, findProfileSkills(application.getJobSeeker()), findResume(application.getJobSeeker()));
        }
        validateStatusTransition(application.getStatus(), status);
        application.setStatus(status);
        jobApplicationRepository.save(application);
        notifyStatusChange(application);
        return toApplicantResponse(application, findProfileSkills(application.getJobSeeker()), findResume(application.getJobSeeker()));
    }

    @Transactional
    public EmployerApplicantResponse updateApplicantNotes(String username, Long applicationId, String notes) {
        User employer = getEmployer(username);
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (!application.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your job applications");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawn applications cannot be modified");
        }
        application.setNotes(InputSanitizer.sanitize(notes, "notes"));
        jobApplicationRepository.save(application);
        return toApplicantResponse(application, findProfileSkills(application.getJobSeeker()), findResume(application.getJobSeeker()));
    }

    @Transactional(readOnly = true)
    public ResumeFilePayload getApplicantResumeFile(String username, Long applicationId) {
        User employer = getEmployer(username);
        JobApplication application = jobApplicationRepository.findByIdAndEmployerId(applicationId, employer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        Resume resume = findResume(application.getJobSeeker());
        if (resume == null || resume.getUploadedFileData() == null || resume.getUploadedFileData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded resume file not found");
        }
        return new ResumeFilePayload(
                resume.getUploadedFileName(),
                resume.getUploadedFileType(),
                resume.getUploadedFileData()
        );
    }

    public List<EmployerJobResponse> getOpenJobsForSeekers() {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN).stream()
                .map(this::toJobResponse)
                .toList();
    }

    private boolean filterBySearch(JobApplication application, String normalizedSearch) {
        if (normalizedSearch.isBlank()) {
            return true;
        }
        String fullName = safe(application.getJobSeeker().getFullName()).toLowerCase(Locale.ROOT);
        String username = safe(application.getJobSeeker().getUsername()).toLowerCase(Locale.ROOT);
        String skills = safe(findProfileSkills(application.getJobSeeker())).toLowerCase(Locale.ROOT);
        String resumeSearch = safe(buildResumeSearch(findResume(application.getJobSeeker()))).toLowerCase(Locale.ROOT);
        return fullName.contains(normalizedSearch)
                || username.contains(normalizedSearch)
                || skills.contains(normalizedSearch)
                || resumeSearch.contains(normalizedSearch);
    }

    private EmployerApplicantResponse toApplicantResponse(JobApplication application, String skills, Resume resume) {
        EmployerApplicantResponse response = new EmployerApplicantResponse();
        response.setApplicationId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setApplicantUsername(application.getJobSeeker().getUsername());
        response.setApplicantFullName(application.getJobSeeker().getFullName());
        response.setApplicantEmail(application.getJobSeeker().getEmail());
        response.setApplicantSkills(skills);
        response.setResumeSummary(buildResumeSummary(resume));
        if (resume != null) {
            response.setResumeFileName(resume.getUploadedFileName());
            response.setResumeFileType(resume.getUploadedFileType());
            response.setResumeFileSize(resume.getUploadedFileSize());
            response.setResumeFileReference(resume.getUploadedFileReference());
        }
        response.setNotes(application.getNotes());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }

    private String findProfileSkills(User jobSeeker) {
        return jobSeekerProfileRepository.findByUser(jobSeeker).map(JobSeekerProfile::getSkills).orElse(null);
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

    private String buildResumeSearch(Resume resume) {
        if (resume == null) {
            return "";
        }
        return String.join(" ",
                safe(resume.getObjective()),
                safe(resume.getSkills()),
                safe(resume.getEducationCsv()),
                safe(resume.getExperienceCsv()),
                safe(resume.getProjectsCsv()),
                safe(resume.getCertificationsCsv())
        );
    }

    private void applyJobRequest(JobPosting job, EmployerJobRequest request, String companyName) {
        job.setCompanyName(companyName);
        job.setTitle(InputSanitizer.require(request.getTitle(), "title"));
        job.setDescription(InputSanitizer.require(request.getDescription(), "description"));
        job.setSkills(InputSanitizer.sanitize(request.getSkills(), "skills"));
        job.setEducation(InputSanitizer.sanitize(request.getEducation(), "education"));
        job.setMaxExperienceYears(request.getMaxExperienceYears());
        job.setLocation(InputSanitizer.require(request.getLocation(), "location"));
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setJobType(InputSanitizer.require(request.getJobType(), "jobType"));
        job.setOpenings(request.getOpenings());
        job.setApplicationDeadline(request.getApplicationDeadline());
    }

    private String resolveCompanyName(User employer, String requestedCompanyName) {
        String fromRequest = InputSanitizer.sanitize(requestedCompanyName, "companyName");
        if (fromRequest != null && !fromRequest.isBlank()) {
            return fromRequest;
        }
        return employerProfileRepository.findByUser(employer)
                .map(EmployerProfile::getCompanyName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(employer.getFullName());
    }

    private void createNotificationsForMatchingSeekers(JobPosting job) {
        Set<String> requiredSkills = tokenize(job.getSkills());
        if (requiredSkills.isEmpty()) {
            return;
        }

        for (JobSeekerProfile profile : jobSeekerProfileRepository.findAll()) {
            User seeker = profile.getUser();
            if (seeker.getRole() != Role.JOB_SEEKER) {
                continue;
            }
            Set<String> seekerSkills = tokenize(profile.getSkills());
            boolean matched = seekerSkills.stream().anyMatch(requiredSkills::contains);
            if (!matched) {
                continue;
            }

            notificationService.createNotification(
                    seeker,
                    job,
                    "New matching job posted: " + job.getTitle() + " at " + safe(job.getCompanyName()),
                    NotificationType.JOB_RECOMMENDATION
            );
        }
    }

    private void notifyEmployerOnJobPosted(User employer, JobPosting job) {
        String postedAt = job.getCreatedAt() == null
                ? "just now"
                : job.getCreatedAt().format(JOB_POSTED_TIME_FORMATTER);
        String message = "Job posted: " + safe(job.getTitle()) + " | Created: " + postedAt;
        notificationService.createNotification(employer, job, message, NotificationType.SYSTEM);
    }

    private void notifyStatusChange(JobApplication application) {
        User seeker = application.getJobSeeker();
        JobPosting job = application.getJob();
        String message = "Your application for " + safe(job.getTitle()) + " is now " + application.getStatus();
        notificationService.createNotification(seeker, job, message, NotificationType.APPLICATION_UPDATE);
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<String> tokens = new LinkedHashSet<>();
        Arrays.stream(value.toLowerCase(Locale.ROOT).split("[,\\s]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .forEach(tokens::add);
        return tokens;
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

    private EmployerCompanyProfileResponse toCompanyProfileResponse(EmployerProfile profile) {
        EmployerCompanyProfileResponse response = new EmployerCompanyProfileResponse();
        response.setCompanyName(profile.getCompanyName());
        response.setIndustry(profile.getIndustry());
        response.setCompanySize(profile.getCompanySize());
        response.setCompanyDescription(profile.getCompanyDescription());
        response.setWebsite(profile.getWebsite());
        response.setCompanyLocation(profile.getCompanyLocation());
        return response;
    }

    private EmployerProfile createDefaultProfile(User employer) {
        EmployerProfile profile = new EmployerProfile();
        profile.setUser(employer);
        profile.setCompanyName(employer.getFullName());
        profile.setCompanyLocation(employer.getLocation());
        return employerProfileRepository.save(profile);
    }

    private JobPosting getEmployerJob(Long jobId, User employer) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own job postings");
        }
        return job;
    }

    private User getEmployer(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.EMPLOYER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employer role required");
        }
        return user;
    }

    private String clean(String value) {
        return InputSanitizer.sanitize(value, "value");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void validateCompanyProfile(EmployerCompanyProfileRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company profile is required");
        }
        String companyName = InputSanitizer.require(request.getCompanyName(), "companyName");
        if (companyName.length() < 3 || companyName.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company name must be 3-100 characters");
        }
        String industry = InputSanitizer.require(request.getIndustry(), "industry");
        if (industry.length() < 3 || industry.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Industry must be 3-120 characters");
        }

        String sizeValue = InputSanitizer.require(request.getCompanySize(), "companySize");
        try {
            int size = Integer.parseInt(sizeValue);
            if (size < 1 || size > 100000) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company size must be between 1 and 100000");
            }
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company size must be a number");
        }

        String website = InputSanitizer.sanitize(request.getWebsite(), "website");
        if (website != null && !isValidUrl(website)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Website must be a valid URL");
        }
    }

    private void validateJobRequest(EmployerJobRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job details are required");
        }
        String title = InputSanitizer.require(request.getTitle(), "title");
        if (title.length() < 5 || title.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be 5-100 characters");
        }
        String description = InputSanitizer.require(request.getDescription(), "description");
        if (description.length() < 50 || description.length() > 5000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 50-5000 characters");
        }
        String skills = InputSanitizer.sanitize(request.getSkills(), "skills");
        if (countTokens(skills) < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least 3 skills are required");
        }
        if (request.getMaxExperienceYears() == null || request.getMaxExperienceYears() < 0 || request.getMaxExperienceYears() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experience must be between 0 and 30 years");
        }
        if (request.getMinSalary() == null || request.getMaxSalary() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary range is required");
        }
        if (request.getMinSalary().compareTo(request.getMaxSalary()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum salary cannot exceed maximum salary");
        }
        if (request.getMinSalary().signum() < 0 || request.getMaxSalary().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be at least 0");
        }
        String jobType = InputSanitizer.require(request.getJobType(), "jobType").toUpperCase(Locale.ROOT);
        if (!Set.of("FULL_TIME", "PART_TIME", "INTERNSHIP", "CONTRACT", "REMOTE").contains(jobType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job type");
        }
        if (request.getApplicationDeadline() != null && !request.getApplicationDeadline().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deadline must be a future date");
        }
        if (request.getOpenings() == null || request.getOpenings() < 1 || request.getOpenings() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Openings must be between 1 and 500");
        }
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus target) {
        if (current == ApplicationStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawn applications cannot change status");
        }
        if (current == ApplicationStatus.APPLIED && target != ApplicationStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must move from APPLIED to UNDER_REVIEW");
        }
        if (current == ApplicationStatus.UNDER_REVIEW
                && target != ApplicationStatus.SHORTLISTED
                && target != ApplicationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must move from UNDER_REVIEW to SHORTLISTED or REJECTED");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private int countTokens(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(value.split("[,\\s]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .count();
    }
}
