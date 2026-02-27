package com.revhire;

import com.revhire.controller.AuthController;
import com.revhire.dto.ApiResponse;
import com.revhire.dto.AuthResponse;
import com.revhire.dto.ChangePasswordRequest;
import com.revhire.dto.LoginRequest;
import com.revhire.dto.ProfileCompletionRequest;
import com.revhire.dto.RegisterRequest;
import com.revhire.entity.Role;
import com.revhire.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_DelegatesToService() {
        RegisterRequest request = new RegisterRequest();
        ApiResponse expected = new ApiResponse("Registration successful");
        when(authService.register(request)).thenReturn(expected);

        ApiResponse actual = authController.register(request);

        assertEquals("Registration successful", actual.getMessage());
        verify(authService).register(request);
    }

    @Test
    void login_DelegatesToService() {
        LoginRequest request = new LoginRequest();
        AuthResponse expected = new AuthResponse(1L, "user", "u@mail.com", "User", "9999999999", "NY", "FRESHER", Role.JOB_SEEKER, "jwt");
        when(authService.login(request, httpRequest, httpResponse)).thenReturn(expected);

        AuthResponse actual = authController.login(request, httpRequest, httpResponse);

        assertEquals("jwt", actual.getToken());
        verify(authService).login(request, httpRequest, httpResponse);
    }

    @Test
    void changePassword_UsesAuthenticationName() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        ApiResponse expected = new ApiResponse("Password changed successfully");
        when(authentication.getName()).thenReturn("alice");
        when(authService.changePassword("alice", request)).thenReturn(expected);

        ApiResponse actual = authController.changePassword(request, authentication);

        assertEquals("Password changed successfully", actual.getMessage());
        verify(authService).changePassword("alice", request);
    }

    @Test
    void updateProfileCompletion_UsesPathUserAndAuthUser() {
        ProfileCompletionRequest request = new ProfileCompletionRequest();
        request.setProfileCompleted(true);
        when(authentication.getName()).thenReturn("alice");
        when(authService.updateProfileCompletion(10L, "alice", request))
                .thenReturn(new ApiResponse("Profile completion updated"));

        ApiResponse actual = authController.updateProfileCompletion(10L, request, authentication);

        assertEquals("Profile completion updated", actual.getMessage());
        verify(authService).updateProfileCompletion(10L, "alice", request);
    }
}
