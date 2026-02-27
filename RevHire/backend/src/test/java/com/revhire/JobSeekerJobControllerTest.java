package com.revhire;

import com.revhire.controller.JobSeekerJobController;
import com.revhire.dto.ApplyRequest;
import com.revhire.dto.WithdrawRequest;
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
class JobSeekerJobControllerTest {

    @Mock
    private JobSeekerJobService jobSeekerJobService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobSeekerJobController jobSeekerJobController;

    @Test
    void apply_ReturnsSuccessMessage() {
        ApplyRequest request = new ApplyRequest();
        when(authentication.getName()).thenReturn("seeker");

        Map<String, String> response = jobSeekerJobController.apply(authentication, 5L, request);

        assertEquals("Application submitted", response.get("message"));
        verify(jobSeekerJobService).applyToJob("seeker", 5L, request);
    }

    @Test
    void withdrawByApplication_ReturnsSuccessMessage() {
        WithdrawRequest request = new WithdrawRequest();
        request.setConfirm(true);
        when(authentication.getName()).thenReturn("seeker");

        Map<String, String> response = jobSeekerJobController.withdrawByApplication(authentication, 12L, request);

        assertEquals("Application withdrawn", response.get("message"));
        verify(jobSeekerJobService).withdrawApplicationById("seeker", 12L, request);
    }
}
