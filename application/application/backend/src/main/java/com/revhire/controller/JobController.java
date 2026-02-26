package com.revhire.controller;

import com.revhire.dto.EmployerJobResponse;
import com.revhire.service.EmployerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final EmployerService employerService;

    public JobController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping
    public List<EmployerJobResponse> getOpenJobs() {
        return employerService.getOpenJobsForSeekers();
    }
}
