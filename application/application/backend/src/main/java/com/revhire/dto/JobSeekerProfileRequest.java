package com.revhire.dto;

import jakarta.validation.constraints.Size;

public class JobSeekerProfileRequest {

    @Size(max = 1000)
    private String skills;

    @Size(max = 1000)
    private String education;

    @Size(max = 1000)
    private String certifications;

    @Size(max = 150)
    private String headline;

    @Size(max = 500)
    private String summary;

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
