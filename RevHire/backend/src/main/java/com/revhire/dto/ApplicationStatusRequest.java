package com.revhire.dto;

import com.revhire.entity.ApplicationStatus;

public class ApplicationStatusRequest {
    private ApplicationStatus status;

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
