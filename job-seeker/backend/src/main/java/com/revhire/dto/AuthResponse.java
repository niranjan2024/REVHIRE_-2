package com.revhire.dto;

import com.revhire.entity.Role;

public class AuthResponse {
    private final Long userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final String mobileNumber;
    private final String location;
    private final Role role;

    public AuthResponse(Long userId,
                        String username,
                        String email,
                        String fullName,
                        String mobileNumber,
                        String location,
                        Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.location = location;
        this.role = role;
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

    public Role getRole() {
        return role;
    }
}
