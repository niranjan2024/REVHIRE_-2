package com.revhire.service;

import com.revhire.dto.JobSeekerProfileRequest;
import com.revhire.dto.JobSeekerProfileResponse;
import com.revhire.entity.JobSeekerProfile;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.JobSeekerProfileRepository;
import com.revhire.repository.UserRepository;
import com.revhire.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
@Service
public class JobSeekerProfileService {

    private final UserRepository userRepository;
    private final JobSeekerProfileRepository profileRepository;

    public JobSeekerProfileService(UserRepository userRepository,
                                   JobSeekerProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public JobSeekerProfileResponse getProfile(String username) {
        User user = getAuthenticatedJobSeeker(username);
        JobSeekerProfile profile = profileRepository.findByUser(user)
                .orElseGet(() -> emptyProfile(user));
        return toResponse(user, profile);
    }

    @Transactional
    public JobSeekerProfileResponse upsertProfile(String username, JobSeekerProfileRequest request) {
        User user = getAuthenticatedJobSeeker(username);
        JobSeekerProfile profile = profileRepository.findByUser(user).orElseGet(() -> {
            JobSeekerProfile newProfile = new JobSeekerProfile();
            newProfile.setUser(user);
            return newProfile;
        });

        String skills = InputSanitizer.require(request.getSkills(), "skills");
        validateSkills(skills);
        profile.setSkills(InputSanitizer.sanitize(skills, "skills"));
        profile.setEducation(InputSanitizer.sanitize(request.getEducation(), "education"));
        profile.setCertifications(InputSanitizer.sanitize(request.getCertifications(), "certifications"));
        profile.setHeadline(InputSanitizer.sanitize(request.getHeadline(), "headline"));
        profile.setSummary(InputSanitizer.sanitize(request.getSummary(), "summary"));
        user.setLocation(InputSanitizer.require(request.getLocation(), "location"));
        user.setEmploymentStatus(request.getEmploymentStatus());
        userRepository.save(user);
        profileRepository.save(profile);

        return toResponse(user, profile);
    }

    private User getAuthenticatedJobSeeker(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "User not found"));
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new com.revhire.exception.ForbiddenException( "Only job seekers can access this module");
        }
        return user;
    }

    private JobSeekerProfile emptyProfile(User user) {
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUser(user);
        return profile;
    }

    private JobSeekerProfileResponse toResponse(User user, JobSeekerProfile profile) {
        JobSeekerProfileResponse response = new JobSeekerProfileResponse();
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setLocation(user.getLocation());
        response.setEmploymentStatus(user.getEmploymentStatus() == null ? null : user.getEmploymentStatus().name());
        response.setSkills(profile.getSkills());
        response.setEducation(profile.getEducation());
        response.setCertifications(profile.getCertifications());
        response.setHeadline(profile.getHeadline());
        response.setSummary(profile.getSummary());
        return response;
    }

    private void validateSkills(String skills) {
        int count = (int) Arrays.stream(skills.split("[,\\s]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .count();
        if (count < 1) {
            throw new com.revhire.exception.BadRequestException( "At least one skill is required");
        }
        if (count > 50) {
            throw new com.revhire.exception.BadRequestException( "Skills cannot exceed 50 items");
        }
    }
}
