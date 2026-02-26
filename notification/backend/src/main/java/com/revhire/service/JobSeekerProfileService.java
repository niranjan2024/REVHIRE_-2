package com.revhire.service;

import com.revhire.dto.JobSeekerProfileRequest;
import com.revhire.dto.JobSeekerProfileResponse;
import com.revhire.entity.JobSeekerProfile;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.JobSeekerProfileRepository;
import com.revhire.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        profile.setSkills(clean(request.getSkills()));
        profile.setEducation(clean(request.getEducation()));
        profile.setCertifications(clean(request.getCertifications()));
        profile.setHeadline(clean(request.getHeadline()));
        profile.setSummary(clean(request.getSummary()));
        profileRepository.save(profile);

        return toResponse(user, profile);
    }

    private User getAuthenticatedJobSeeker(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can access this module");
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
        response.setSkills(profile.getSkills());
        response.setEducation(profile.getEducation());
        response.setCertifications(profile.getCertifications());
        response.setHeadline(profile.getHeadline());
        response.setSummary(profile.getSummary());
        return response;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
