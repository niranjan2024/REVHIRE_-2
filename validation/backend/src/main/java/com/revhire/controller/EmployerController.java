package com.revhire.controller;

import com.revhire.dto.ApplicationNotesRequest;
import com.revhire.dto.ApplicationStatusRequest;
import com.revhire.dto.EmployerApplicantResponse;
import com.revhire.dto.EmployerCompanyProfileRequest;
import com.revhire.dto.EmployerCompanyProfileResponse;
import com.revhire.dto.EmployerJobRequest;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.EmployerStatisticsResponse;
import com.revhire.entity.ApplicationStatus;
import com.revhire.entity.JobStatus;
import com.revhire.service.EmployerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping("/company-profile")
    public EmployerCompanyProfileResponse getCompanyProfile(Authentication authentication) {
        return employerService.getCompanyProfile(authentication.getName());
    }

    @PutMapping("/company-profile")
    public EmployerCompanyProfileResponse updateCompanyProfile(@Valid @RequestBody EmployerCompanyProfileRequest request,
                                                               Authentication authentication) {
        return employerService.updateCompanyProfile(authentication.getName(), request);
    }

    @GetMapping("/jobs")
    public List<EmployerJobResponse> getJobs(Authentication authentication) {
        return employerService.getEmployerJobs(authentication.getName());
    }

    @PostMapping("/jobs")
    public EmployerJobResponse createJob(@Valid @RequestBody EmployerJobRequest request,
                                         Authentication authentication) {
        return employerService.createJob(authentication.getName(), request);
    }

    @PutMapping("/jobs/{jobId}")
    public EmployerJobResponse updateJob(@PathVariable Long jobId,
                                         @Valid @RequestBody EmployerJobRequest request,
                                         Authentication authentication) {
        return employerService.updateJob(authentication.getName(), jobId, request);
    }

    @DeleteMapping("/jobs/{jobId}")
    public void deleteJob(@PathVariable Long jobId, Authentication authentication) {
        employerService.deleteJob(authentication.getName(), jobId);
    }

    @PatchMapping("/jobs/{jobId}/close")
    public EmployerJobResponse closeJob(@PathVariable Long jobId, Authentication authentication) {
        return employerService.setJobStatus(authentication.getName(), jobId, JobStatus.CLOSED);
    }

    @PatchMapping("/jobs/{jobId}/reopen")
    public EmployerJobResponse reopenJob(@PathVariable Long jobId, Authentication authentication) {
        return employerService.setJobStatus(authentication.getName(), jobId, JobStatus.OPEN);
    }

    @PatchMapping("/jobs/{jobId}/fill")
    public EmployerJobResponse fillJob(@PathVariable Long jobId, Authentication authentication) {
        return employerService.setJobStatus(authentication.getName(), jobId, JobStatus.FILLED);
    }

    @GetMapping("/statistics")
    public EmployerStatisticsResponse getStatistics(Authentication authentication) {
        return employerService.getStatistics(authentication.getName());
    }

    @GetMapping("/applicants")
    public List<EmployerApplicantResponse> getApplicants(@RequestParam(required = false) ApplicationStatus status,
                                                         @RequestParam(required = false) String search,
                                                         Authentication authentication) {
        return employerService.getApplicants(authentication.getName(), status, search);
    }

    @PatchMapping("/applicants/{applicationId}/status")
    public EmployerApplicantResponse updateApplicantStatus(@PathVariable Long applicationId,
                                                           @RequestBody ApplicationStatusRequest request,
                                                           Authentication authentication) {
        return employerService.updateApplicantStatus(authentication.getName(), applicationId, request.getStatus());
    }

    @PatchMapping("/applicants/{applicationId}/shortlist")
    public EmployerApplicantResponse shortlist(@PathVariable Long applicationId,
                                               Authentication authentication) {
        return employerService.updateApplicantStatus(authentication.getName(), applicationId, ApplicationStatus.SHORTLISTED);
    }

    @PatchMapping("/applicants/{applicationId}/reject")
    public EmployerApplicantResponse reject(@PathVariable Long applicationId,
                                            Authentication authentication) {
        return employerService.updateApplicantStatus(authentication.getName(), applicationId, ApplicationStatus.REJECTED);
    }

    @PatchMapping("/applicants/{applicationId}/under-review")
    public EmployerApplicantResponse underReview(@PathVariable Long applicationId,
                                                 Authentication authentication) {
        return employerService.updateApplicantStatus(authentication.getName(), applicationId, ApplicationStatus.UNDER_REVIEW);
    }

    @PatchMapping("/applicants/{applicationId}/notes")
    public EmployerApplicantResponse updateApplicantNotes(@PathVariable Long applicationId,
                                                          @RequestBody ApplicationNotesRequest request,
                                                          Authentication authentication) {
        return employerService.updateApplicantNotes(authentication.getName(), applicationId, request.getNotes());
    }
}
