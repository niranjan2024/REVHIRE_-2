package com.revhire;

import com.revhire.controller.EmployerController;
import com.revhire.dto.EmployerJobResponse;
import com.revhire.dto.ResumeFilePayload;
import com.revhire.entity.JobStatus;
import com.revhire.service.EmployerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerControllerTest {

    @Mock
    private EmployerService employerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployerController employerController;

    @Test
    void getJobs_ReturnsEmployerJobs() {
        EmployerJobResponse job = new EmployerJobResponse();
        job.setId(1L);

        when(authentication.getName()).thenReturn("employer");
        when(employerService.getEmployerJobs("employer")).thenReturn(List.of(job));

        List<EmployerJobResponse> jobs = employerController.getJobs(authentication);

        assertEquals(1, jobs.size());
        assertEquals(1L, jobs.get(0).getId());
    }

    @Test
    void closeJob_UsesClosedStatus() {
        EmployerJobResponse response = new EmployerJobResponse();
        response.setStatus(JobStatus.CLOSED);

        when(authentication.getName()).thenReturn("employer");
        when(employerService.setJobStatus("employer", 7L, JobStatus.CLOSED)).thenReturn(response);

        EmployerJobResponse actual = employerController.closeJob(7L, authentication);

        assertEquals(JobStatus.CLOSED, actual.getStatus());
        verify(employerService).setJobStatus("employer", 7L, JobStatus.CLOSED);
    }

    @Test
    void downloadApplicantResume_FallsBackToOctetStreamForInvalidType() {
        ResumeFilePayload payload = new ResumeFilePayload("cv.pdf", "invalid/type/", "data".getBytes());

        when(authentication.getName()).thenReturn("employer");
        when(employerService.getApplicantResumeFile("employer", 100L)).thenReturn(payload);

        var response = employerController.downloadApplicantResume(100L, authentication);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertArrayEquals("data".getBytes(), response.getBody());
    }
}
