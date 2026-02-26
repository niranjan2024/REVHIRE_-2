package com.revhire.service;

import com.revhire.dto.EmployerApplicantResponse;
import com.revhire.dto.EmployerCompanyProfileRequest;
import com.revhire.dto.EmployerCompanyProfileResponse;
import com.revhire.dto.EmployerJobRequest;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.EmployerStatisticsResponse;
import com.revhire.entity.ApplicationStatus;
import com.revhire.entity.EmployerProfile;
import com.revhire.entity.JobApplication;
import com.revhire.entity.JobPosting;
import com.revhire.entity.JobSeekerProfile;
import com.revhire.entity.JobStatus;
import com.revhire.entity.Notification;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.EmployerProfileRepository;
import com.revhire.repository.JobApplicationRepository;
import com.revhire.repository.JobPostingRepository;
import com.revhire.repository.JobSeekerProfileRepository;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EmployerService {

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final NotificationRepository notificationRepository;

    public EmployerService(UserRepository userRepository,
                           EmployerProfileRepository employerProfileRepository,
                           JobPostingRepository jobPostingRepository,
                           JobApplicationRepository jobApplicationRepository,
                           JobSeekerProfileRepository jobSeekerProfileRepository,
                           NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.notificationRepository = notificationRepository;
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

        profile.setCompanyName(clean(request.getCompanyName()));
        profile.setIndustry(clean(request.getIndustry()));
        profile.setCompanySize(clean(request.getCompanySize()));
        profile.setCompanyDescription(clean(request.getCompanyDescription()));
        profile.setWebsite(clean(request.getWebsite()));
        profile.setCompanyLocation(clean(request.getCompanyLocation()));
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
        validateSalaryRange(request);
        User employer = getEmployer(username);

        JobPosting job = new JobPosting();
        job.setEmployer(employer);
        applyJobRequest(job, request, resolveCompanyName(employer, request.getCompanyName()));
        job.setStatus(JobStatus.OPEN);
        JobPosting saved = jobPostingRepository.save(job);

        createNotificationsForMatchingSeekers(saved);
        return toJobResponse(saved);
    }

    @Transactional
    public EmployerJobResponse updateJob(String username, Long jobId, EmployerJobRequest request) {
        validateSalaryRange(request);
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
                .map(app -> toApplicantResponse(app, findProfileSkills(app.getJobSeeker())))
                .toList();
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
        return fullName.contains(normalizedSearch)
                || username.contains(normalizedSearch)
                || skills.contains(normalizedSearch);
    }

    private EmployerApplicantResponse toApplicantResponse(JobApplication application, String skills) {
        EmployerApplicantResponse response = new EmployerApplicantResponse();
        response.setApplicationId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setApplicantUsername(application.getJobSeeker().getUsername());
        response.setApplicantFullName(application.getJobSeeker().getFullName());
        response.setApplicantEmail(application.getJobSeeker().getEmail());
        response.setApplicantSkills(skills);
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }

    private String findProfileSkills(User jobSeeker) {
        return jobSeekerProfileRepository.findByUser(jobSeeker).map(JobSeekerProfile::getSkills).orElse(null);
    }

    private void validateSalaryRange(EmployerJobRequest request) {
        if (request.getMinSalary().compareTo(request.getMaxSalary()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum salary cannot exceed maximum salary");
        }
    }

    private void applyJobRequest(JobPosting job, EmployerJobRequest request, String companyName) {
        job.setCompanyName(companyName);
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setSkills(clean(request.getSkills()));
        job.setEducation(clean(request.getEducation()));
        job.setMaxExperienceYears(request.getMaxExperienceYears());
        job.setLocation(request.getLocation().trim());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setJobType(request.getJobType().trim());
        job.setOpenings(request.getOpenings());
        job.setApplicationDeadline(request.getApplicationDeadline());
    }

    private String resolveCompanyName(User employer, String requestedCompanyName) {
        String fromRequest = clean(requestedCompanyName);
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

        List<Notification> notifications = new ArrayList<>();
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

            Notification notification = new Notification();
            notification.setRecipient(seeker);
            notification.setJob(job);
            notification.setMessage("New matching job posted: " + job.getTitle() + " at " + safe(job.getCompanyName()));
            notifications.add(notification);
        }

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
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
        return value == null ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
