package com.revhire;

import com.revhire.config.CustomUserDetailsService;
import com.revhire.dto.ApiResponse;
import com.revhire.dto.ForgotPasswordRequest;
import com.revhire.dto.ForgotPasswordResponse;
import com.revhire.dto.LoginRequest;
import com.revhire.dto.ProfileCompletionRequest;
import com.revhire.dto.RegisterRequest;
import com.revhire.dto.ResetPasswordRequest;
import com.revhire.entity.EmploymentStatus;
import com.revhire.entity.PasswordResetToken;
import com.revhire.entity.Role;
import com.revhire.entity.User;
import com.revhire.repository.PasswordResetTokenRepository;
import com.revhire.repository.UserRepository;
import com.revhire.security.JwtService;
import com.revhire.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_SavesUserAndReturnsSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@mail.com");
        request.setPassword("Aa@12345");
        request.setConfirmPassword("Aa@12345");
        request.setFullName("Alice User");
        request.setMobileNumber("9999999999");
        request.setSecurityQuestion("pet");
        request.setSecurityAnswer("cat");
        request.setLocation("NY");
        request.setRole(Role.JOB_SEEKER);
        request.setEmploymentStatus(EmploymentStatus.FRESHER);

        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("Aa@12345")).thenReturn("encoded");
        when(passwordEncoder.encode("cat")).thenReturn("encodedAnswer");

        ApiResponse response = authService.register(request);

        assertEquals("Registration successful", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ReturnsTokenForValidCredentials() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@mail.com");
        user.setFullName("Alice");
        user.setMobileNumber("9999999999");
        user.setLocation("NY");
        user.setRole(Role.JOB_SEEKER);
        user.setEmploymentStatus(EmploymentStatus.FRESHER);
        user.setPassword("encoded");

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("alice");
        request.setPassword("Aa@12345");

        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("alice")
                .password("encoded")
                .authorities("ROLE_JOB_SEEKER")
                .build();

        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Aa@12345", "encoded")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(principal);
        when(jwtService.generateToken(eq(principal), any(Map.class))).thenReturn("jwt-token");

        var response = authService.login(request, null, null);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void forgotPassword_CreatesResetToken() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("alice@mail.com");
        User user = new User();
        user.setEmail("alice@mail.com");

        when(userRepository.findByEmailIgnoreCase("alice@mail.com")).thenReturn(Optional.of(user));

        ForgotPasswordResponse response = authService.forgotPassword(request);

        assertEquals("Reset token generated", response.getMessage());
        assertNotNull(response.getResetToken());
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_RejectsExpiredToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("tkn");
        request.setNewPassword("Aa@12345");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("tkn");
        token.setExpiryAt(LocalDateTime.now().minusMinutes(1));
        token.setUser(new User());

        when(tokenRepository.findByToken("tkn")).thenReturn(Optional.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfileCompletion_RejectsDifferentUser() {
        ProfileCompletionRequest request = new ProfileCompletionRequest();
        request.setProfileCompleted(true);

        User actor = new User();
        actor.setId(1L);
        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(actor));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.updateProfileCompletion(2L, "alice", request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
