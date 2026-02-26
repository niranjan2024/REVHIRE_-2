package com.revhire.service;

import com.revhire.dto.ResumeRequest;
import com.revhire.dto.ResumeResponse;
import com.revhire.dto.ResumeUploadResponse;
import com.revhire.entity.Resume;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.ResumeRepository;
import com.revhire.repository.UserRepository;
import com.revhire.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".docx");
    private static final int MIN_YEAR = 1980;
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
    private static final Pattern YEARS_PATTERN = Pattern.compile("(?i)(\\d+)\\s*years?");

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

        validateTextResume(request);
        resume.setObjective(InputSanitizer.require(request.getObjective(), "objective"));
        resume.setEducationCsv(toCsv(request.getEducation()));
        resume.setExperienceCsv(toCsv(request.getExperience()));
        resume.setProjectsCsv(toCsv(request.getProjects()));
        resume.setCertificationsCsv(toCsv(request.getCertifications()));
        resume.setSkills(InputSanitizer.sanitize(request.getSkills(), "skills"));
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed formats: pdf, docx");
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
        return InputSanitizer.sanitize(value, "value");
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

    private void validateTextResume(ResumeRequest request) {
        String objective = InputSanitizer.require(request.getObjective(), "objective");
        if (objective.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Objective must be up to 500 characters");
        }

        String education = InputSanitizer.require(request.getEducation(), "education");
        validateEducationEntries(education);

        String experience = InputSanitizer.sanitize(request.getExperience(), "experience");
        validateExperienceEntries(experience);

        String projects = InputSanitizer.sanitize(request.getProjects(), "projects");
        validateProjectEntries(projects);
    }

    private void validateEducationEntries(String education) {
        String[] entries = education.split(",");
        int currentYear = LocalDate.now().getYear();
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!trimmed.matches(".*[A-Za-z].*")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Education entry must include a degree");
            }
            Matcher matcher = YEAR_PATTERN.matcher(trimmed);
            if (!matcher.find()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Education entry must include a year");
            }
            int year = Integer.parseInt(matcher.group(1));
            if (year < MIN_YEAR || year > currentYear) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Education year must be between 1980 and current year");
            }
        }
    }

    private void validateExperienceEntries(String experience) {
        if (experience == null || experience.isBlank()) {
            return;
        }
        String[] entries = experience.split(",");
        int currentYear = LocalDate.now().getYear();
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher yearsMatcher = YEARS_PATTERN.matcher(trimmed);
            if (yearsMatcher.find()) {
                int years = Integer.parseInt(yearsMatcher.group(1));
                if (years < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experience years must be at least 0");
                }
            }
            Matcher matcher = YEAR_PATTERN.matcher(trimmed);
            int[] years = matcher.results()
                    .limit(2)
                    .mapToInt(result -> Integer.parseInt(result.group(1)))
                    .toArray();
            if (years.length >= 2) {
                int start = years[0];
                int end = years[1];
                if (start > end) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experience end year must be after start year");
                }
                if (start < MIN_YEAR || end > currentYear) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Experience years must be between 1980 and current year");
                }
            }
        }
    }

    private void validateProjectEntries(String projects) {
        if (projects == null || projects.isBlank()) {
            return;
        }
        long count = Arrays.stream(projects.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .count();
        if (count > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projects cannot exceed 5 entries");
        }
    }
}
