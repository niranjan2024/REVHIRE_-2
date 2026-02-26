package com.revhire.controller;

import com.revhire.dto.ResumeRequest;
import com.revhire.dto.ResumeResponse;
import com.revhire.dto.ResumeUploadResponse;
import com.revhire.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    public ResumeResponse getResume(Authentication authentication) {
        return resumeService.getResume(authentication.getName());
    }

    @PutMapping
    public ResumeResponse updateResume(Authentication authentication,
                                       @Valid @RequestBody ResumeRequest request) {
        return resumeService.upsertResume(authentication.getName(), request);
    }

    @PostMapping("/upload")
    public ResumeUploadResponse uploadFormattedResume(Authentication authentication,
                                                      @RequestPart("file") MultipartFile file) {
        return resumeService.uploadFormattedResume(authentication.getName(), file);
    }
}
