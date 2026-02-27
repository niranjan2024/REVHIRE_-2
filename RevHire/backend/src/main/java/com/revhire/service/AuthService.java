package com.revhire.service;

import com.revhire.config.CustomUserDetailsService;
import com.revhire.dto.ApiResponse;
import com.revhire.dto.AuthResponse;
import com.revhire.dto.ChangePasswordRequest;
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
import com.revhire.util.LegacyPasswordUtil;
import com.revhire.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       CustomUserDetailsService userDetailsService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public ApiResponse register(RegisterRequest request) {
        String username = InputSanitizer.require(request.getUsername(), "username");
        String email = InputSanitizer.require(request.getEmail(), "email").toLowerCase();
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new com.revhire.exception.BadRequestException( "Password and confirm password do not match");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new com.revhire.exception.ConflictException( "Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new com.revhire.exception.ConflictException( "Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null ? Role.JOB_SEEKER : request.getRole());
        user.setFullName(InputSanitizer.require(request.getFullName(), "name"));
        user.setMobileNumber(InputSanitizer.require(request.getMobileNumber(), "phone"));
        user.setSecurityQuestion(InputSanitizer.require(request.getSecurityQuestion(), "securityQuestion"));
        user.setSecurityAnswer(passwordEncoder.encode(InputSanitizer.require(request.getSecurityAnswer(), "securityAnswer")));
        user.setLocation(InputSanitizer.require(request.getLocation(), "location"));
        user.setEmploymentStatus(request.getEmploymentStatus() == null ? EmploymentStatus.FRESHER : request.getEmploymentStatus());
        userRepository.save(user);

        return new ApiResponse("Registration successful");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String identifier = InputSanitizer.require(request.getUsernameOrEmail(), "emailOrUsername");
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!isPasswordValidAndUpgradeIfLegacy(user, request.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserDetails principal = userDetailsService.loadUserByUsername(user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole() == null ? null : user.getRole().name());
        claims.put("userId", user.getId());
        String token = jwtService.generateToken(principal, claims);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getMobileNumber(),
                user.getLocation(),
                user.getEmploymentStatus() == null ? null : user.getEmploymentStatus().name(),
                user.getRole(),
                token
        );
    }

    public ApiResponse logout(HttpServletRequest request, HttpServletResponse response) {
        return new ApiResponse("Logout successful. Discard token on client side.");
    }

    public ApiResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "User not found"));

        boolean oldMatches = passwordEncoder.matches(request.getOldPassword(), user.getPassword())
                || LegacyPasswordUtil.sha256Hex(request.getOldPassword()).equalsIgnoreCase(user.getPassword());
        if (!oldMatches) {
            throw new com.revhire.exception.BadRequestException( "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new ApiResponse("Password changed successfully");
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "Email not found"));

        tokenRepository.deleteByUser(user);
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryAt(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(token);

        return new ForgotPasswordResponse("Reset token generated", token.getToken());
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new com.revhire.exception.BadRequestException( "Invalid token"));

        if (token.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new com.revhire.exception.BadRequestException( "Token expired");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.delete(token);
        return new ApiResponse("Password reset successful");
    }

    public ApiResponse updateProfileCompletion(Long userId, String currentUsername, ProfileCompletionRequest request) {
        User actor = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new com.revhire.exception.NotFoundException( "Current user not found"));

        if (!actor.getId().equals(userId)) {
            throw new com.revhire.exception.ForbiddenException( "You can only update your own profile status");
        }

        actor.setProfileCompleted(Boolean.TRUE.equals(request.getProfileCompleted()));
        userRepository.save(actor);
        return new ApiResponse("Profile completion updated");
    }

    private boolean isPasswordValidAndUpgradeIfLegacy(User user, String rawPassword) {
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return true;
        }

        String legacySha = LegacyPasswordUtil.sha256Hex(rawPassword);
        if (legacySha.equalsIgnoreCase(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
