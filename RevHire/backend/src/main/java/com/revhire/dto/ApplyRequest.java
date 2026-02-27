package com.revhire.dto;

import jakarta.validation.constraints.Size;

public class ApplyRequest {

    @Size(max = 1000)
    private String coverLetter;

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}
