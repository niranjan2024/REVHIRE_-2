package com.revhire.controller;

import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.JobSeekerApplicationResponse;
import com.revhire.service.JobSeekerJobService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobseeker/jobs")
public class JobSeekerJobController {

    private final JobSeekerJobService jobSeekerJobService;

    public JobSeekerJobController(JobSeekerJobService jobSeekerJobService) {
        this.jobSeekerJobService = jobSeekerJobService;
    }

    @GetMapping("/search")
    public List<EmployerJobResponse> searchJobs(Authentication authentication,
                                                @RequestParam(required = false) String title,
                                                @RequestParam(required = false) String location,
                                                @RequestParam(required = false) String company,
                                                @RequestParam(required = false) String jobType,
                                                @RequestParam(required = false) Integer maxExperienceYears,
                                                @RequestParam(required = false) BigDecimal minSalary,
                                                @RequestParam(required = false) BigDecimal maxSalary) {
        return jobSeekerJobService.searchJobs(authentication.getName(), title, location, company, jobType,
                maxExperienceYears, minSalary, maxSalary);
    }

    @GetMapping("/applied")
    public List<Long> getAppliedJobs(Authentication authentication) {
        return jobSeekerJobService.getAppliedJobIds(authentication.getName());
    }

    @GetMapping("/applications")
    public List<JobSeekerApplicationResponse> getApplications(Authentication authentication) {
        return jobSeekerJobService.getApplications(authentication.getName());
    }

    @PostMapping("/{jobId}/apply")
    public Map<String, String> apply(Authentication authentication, @PathVariable Long jobId) {
        jobSeekerJobService.applyToJob(authentication.getName(), jobId);
        return Map.of("message", "Application submitted");
    }

    @PostMapping("/{jobId}/withdraw")
    public Map<String, String> withdraw(Authentication authentication, @PathVariable Long jobId) {
        jobSeekerJobService.withdrawApplication(authentication.getName(), jobId);
        return Map.of("message", "Application withdrawn");
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    public Map<String, String> withdrawByApplication(Authentication authentication, @PathVariable Long applicationId) {
        jobSeekerJobService.withdrawApplicationById(authentication.getName(), applicationId);
        return Map.of("message", "Application withdrawn");
    }
}
