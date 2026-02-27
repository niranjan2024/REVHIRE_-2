package com.revhire;

import com.revhire.controller.ApplicationController;
import com.revhire.dto.ApplicationApplyRequest;
import com.revhire.dto.ApplicationStatusRequest;
import com.revhire.dto.EmployerApplicantResponse;
import com.revhire.entity.ApplicationStatus;
import com.revhire.service.EmployerService;
import com.revhire.service.JobSeekerJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private JobSeekerJobService jobSeekerJobService;

    @Mock
    private EmployerService employerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ApplicationController applicationController;

    @Test
    void apply_SubmitsApplication() {
        ApplicationApplyRequest request = new ApplicationApplyRequest();
        request.setJobId(10L);
        when(authentication.getName()).thenReturn("seeker");

        Map<String, String> response = applicationController.apply(authentication, request);

        assertEquals("Application submitted", response.get("message"));
        verify(jobSeekerJobService).applyToJob("seeker", 10L, request);
    }

    @Test
    void updateStatus_DelegatesToEmployerService() {
        ApplicationStatusRequest request = new ApplicationStatusRequest();
        request.setStatus(ApplicationStatus.UNDER_REVIEW);
        EmployerApplicantResponse expected = new EmployerApplicantResponse();
        expected.setApplicationId(99L);

        when(authentication.getName()).thenReturn("emp");
        when(employerService.updateApplicantStatus("emp", 99L, ApplicationStatus.UNDER_REVIEW)).thenReturn(expected);

        EmployerApplicantResponse actual = applicationController.updateStatus(authentication, 99L, request);

        assertEquals(99L, actual.getApplicationId());
        verify(employerService).updateApplicantStatus("emp", 99L, ApplicationStatus.UNDER_REVIEW);
    }
}
