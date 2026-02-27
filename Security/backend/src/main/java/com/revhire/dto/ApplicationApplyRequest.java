package com.revhire.dto;

import jakarta.validation.constraints.NotNull;
public class ApplicationApplyRequest extends ApplyRequest {

    @NotNull
    private Long jobId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

}
