package com.revhire.controller;

import com.revhire.dto.ApplicationApplyRequest;
import com.revhire.dto.ApplicationStatusRequest;
import com.revhire.dto.EmployerApplicantResponse;
import com.revhire.service.EmployerService;
import com.revhire.service.JobSeekerJobService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final JobSeekerJobService jobSeekerJobService;
    private final EmployerService employerService;

    public ApplicationController(JobSeekerJobService jobSeekerJobService,
                                 EmployerService employerService) {
        this.jobSeekerJobService = jobSeekerJobService;
        this.employerService = employerService;
    }

    @PostMapping("/apply")
    public Map<String, String> apply(Authentication authentication,
                                     @Valid @RequestBody ApplicationApplyRequest request) {
        jobSeekerJobService.applyToJob(authentication.getName(), request.getJobId(), request);
        return Map.of("message", "Application submitted");
    }

    @PatchMapping("/{applicationId}/status")
    public EmployerApplicantResponse updateStatus(Authentication authentication,
                                                  @PathVariable Long applicationId,
                                                  @Valid @RequestBody ApplicationStatusRequest request) {
        return employerService.updateApplicantStatus(authentication.getName(), applicationId, request.getStatus());
    }
}
