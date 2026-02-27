package com.revhire;

import com.revhire.controller.JobSeekerProfileController;
import com.revhire.dto.JobSeekerProfileRequest;
import com.revhire.dto.JobSeekerProfileResponse;
import com.revhire.service.JobSeekerProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSeekerProfileControllerTest {

    @Mock
    private JobSeekerProfileService profileService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobSeekerProfileController controller;

    @Test
    void getProfile_DelegatesToService() {
        JobSeekerProfileResponse response = new JobSeekerProfileResponse();
        response.setUsername("seeker");

        when(authentication.getName()).thenReturn("seeker");
        when(profileService.getProfile("seeker")).thenReturn(response);

        JobSeekerProfileResponse actual = controller.getProfile(authentication);

        assertEquals("seeker", actual.getUsername());
    }

    @Test
    void updateProfile_DelegatesToService() {
        JobSeekerProfileRequest request = new JobSeekerProfileRequest();
        JobSeekerProfileResponse response = new JobSeekerProfileResponse();
        response.setLocation("NY");

        when(authentication.getName()).thenReturn("seeker");
        when(profileService.upsertProfile("seeker", request)).thenReturn(response);

        JobSeekerProfileResponse actual = controller.updateProfile(authentication, request);

        assertEquals("NY", actual.getLocation());
        verify(profileService).upsertProfile("seeker", request);
    }
}
