package com.revhire.dto;

import com.revhire.entity.Role;

public class AuthResponse {
    private final Long userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final String mobileNumber;
    private final String location;
    private final String employmentStatus;
    private final Role role;
    private final String token;

    public AuthResponse(Long userId,
                        String username,
                        String email,
                        String fullName,
                        String mobileNumber,
                        String location,
                        String employmentStatus,
                        Role role,
                        String token) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.location = location;
        this.employmentStatus = employmentStatus;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getLocation() {
        return location;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public Role getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
