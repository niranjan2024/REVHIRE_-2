package com.revhire;

import com.revhire.dto.JobSeekerProfileRequest;
import com.revhire.dto.JobSeekerProfileResponse;
import com.revhire.entity.EmploymentStatus;
import com.revhire.entity.JobSeekerProfile;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.JobSeekerProfileRepository;
import com.revhire.repository.UserRepository;
import com.revhire.service.JobSeekerProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.revhire.exception.ApiException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSeekerProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerProfileRepository profileRepository;

    @InjectMocks
    private JobSeekerProfileService profileService;

    @Test
    void getProfile_ReturnsEmptyProfileWhenMissing() {
        User user = new User();
        user.setUsername("seeker");
        user.setRole(Role.JOB_SEEKER);

        when(userRepository.findByUsernameIgnoreCase("seeker")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        JobSeekerProfileResponse response = profileService.getProfile("seeker");

        assertEquals("seeker", response.getUsername());
    }

    @Test
    void upsertProfile_UpdatesUserAndProfile() {
        User user = new User();
        user.setUsername("seeker");
        user.setRole(Role.JOB_SEEKER);

        JobSeekerProfileRequest request = new JobSeekerProfileRequest();
        request.setSkills("java,spring");
        request.setLocation("NY");
        request.setEmploymentStatus(EmploymentStatus.EMPLOYED);

        when(userRepository.findByUsernameIgnoreCase("seeker")).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(new JobSeekerProfile()));

        JobSeekerProfileResponse response = profileService.upsertProfile("seeker", request);

        assertEquals("java,spring", response.getSkills());
        verify(userRepository).save(user);
        verify(profileRepository).save(any(JobSeekerProfile.class));
    }

    @Test
    void getProfile_RejectsNonJobSeeker() {
        User user = new User();
        user.setRole(Role.EMPLOYER);
        when(userRepository.findByUsernameIgnoreCase("employer")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class,
                () -> profileService.getProfile("employer"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}
