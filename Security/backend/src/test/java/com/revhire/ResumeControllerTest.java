package com.revhire;

import com.revhire.controller.ResumeController;
import com.revhire.dto.ResumeRequest;
import com.revhire.dto.ResumeResponse;
import com.revhire.dto.ResumeUploadResponse;
import com.revhire.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    @Mock
    private ResumeService resumeService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ResumeController resumeController;

    @Test
    void getResume_DelegatesToService() {
        ResumeResponse expected = new ResumeResponse();
        expected.setObjective("Build products");

        when(authentication.getName()).thenReturn("seeker");
        when(resumeService.getResume("seeker")).thenReturn(expected);

        ResumeResponse actual = resumeController.getResume(authentication);

        assertEquals("Build products", actual.getObjective());
    }

    @Test
    void updateResume_DelegatesToService() {
        ResumeRequest request = new ResumeRequest();
        ResumeResponse expected = new ResumeResponse();
        expected.setSkills("java,spring");

        when(authentication.getName()).thenReturn("seeker");
        when(resumeService.upsertResume("seeker", request)).thenReturn(expected);

        ResumeResponse actual = resumeController.updateResume(authentication, request);

        assertEquals("java,spring", actual.getSkills());
        verify(resumeService).upsertResume("seeker", request);
    }

    @Test
    void uploadFormattedResume_DelegatesToService() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "data".getBytes());
        ResumeUploadResponse expected = new ResumeUploadResponse("ok", "resume.pdf", "application/pdf", 4L, "ref");

        when(authentication.getName()).thenReturn("seeker");
        when(resumeService.uploadFormattedResume("seeker", file)).thenReturn(expected);

        ResumeUploadResponse actual = resumeController.uploadFormattedResume(authentication, file);

        assertEquals("ok", actual.getMessage());
    }
}
