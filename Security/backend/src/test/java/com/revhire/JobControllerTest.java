package com.revhire;

import com.revhire.controller.JobController;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.service.EmployerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private EmployerService employerService;

    @InjectMocks
    private JobController jobController;

    @Test
    void getOpenJobs_ReturnsServiceData() {
        EmployerJobResponse job = new EmployerJobResponse();
        job.setTitle("Java Developer");
        when(employerService.getOpenJobsForSeekers()).thenReturn(List.of(job));

        List<EmployerJobResponse> jobs = jobController.getOpenJobs();

        assertEquals(1, jobs.size());
        assertEquals("Java Developer", jobs.get(0).getTitle());
    }
}
