package com.revhire.service;

import com.revhire.dto.ResumeRequest;
import com.revhire.dto.ResumeResponse;
import com.revhire.dto.ResumeUploadResponse;
import com.revhire.entity.Resume;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.ResumeRepository;
import com.revhire.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx");

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    public ResumeService(UserRepository userRepository,
                         ResumeRepository resumeRepository) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(String username) {
        User user = getAuthenticatedJobSeeker(username);
        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> emptyResume(user));
        return toResponse(resume);
    }

    @Transactional
    public ResumeResponse upsertResume(String username, ResumeRequest request) {
        User user = getAuthenticatedJobSeeker(username);
        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> {
            Resume newResume = new Resume();
            newResume.setUser(user);
            return newResume;
        });

        resume.setObjective(clean(request.getObjective()));
        resume.setEducationCsv(toCsv(request.getEducation()));
        resume.setExperienceCsv(toCsv(request.getExperience()));
        resume.setProjectsCsv(toCsv(request.getProjects()));
        resume.setCertificationsCsv(toCsv(request.getCertifications()));
        resume.setSkills(clean(request.getSkills()));
        resumeRepository.save(resume);
        return toResponse(resume);
    }

    @Transactional
    public ResumeUploadResponse uploadFormattedResume(String username, MultipartFile file) {
        User user = getAuthenticatedJobSeeker(username);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must be up to 2MB");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        String contentType = file.getContentType() == null ? "" : file.getContentType().trim();
        if (!isAllowedFile(originalName, contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed formats: pdf, doc, docx");
        }

        Resume resume = resumeRepository.findByUser(user).orElseGet(() -> {
            Resume newResume = new Resume();
            newResume.setUser(user);
            return newResume;
        });
        resume.setUploadedFileName(originalName);
        resume.setUploadedFileType(contentType);
        resume.setUploadedFileSize(file.getSize());
        resumeRepository.save(resume);

        return new ResumeUploadResponse("Resume file uploaded successfully", originalName, contentType, file.getSize());
    }

    private boolean isAllowedFile(String originalName, String contentType) {
        String lowerName = originalName.toLowerCase();
        boolean extensionAllowed = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        boolean contentTypeAllowed = ALLOWED_TYPES.contains(contentType);
        return extensionAllowed && contentTypeAllowed;
    }

    private User getAuthenticatedJobSeeker(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can access this module");
        }
        return user;
    }

    private Resume emptyResume(User user) {
        Resume resume = new Resume();
        resume.setUser(user);
        return resume;
    }

    private ResumeResponse toResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setObjective(resume.getObjective());
        response.setEducation(resume.getEducationCsv());
        response.setExperience(resume.getExperienceCsv());
        response.setProjects(resume.getProjectsCsv());
        response.setCertifications(resume.getCertificationsCsv());
        response.setSkills(resume.getSkills());
        response.setUploadedFileName(resume.getUploadedFileName());
        response.setUploadedFileType(resume.getUploadedFileType());
        response.setUploadedFileSize(resume.getUploadedFileSize());
        return response;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toCsv(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(","));
    }
}
