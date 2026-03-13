package com.revhire.dto;

import jakarta.validation.constraints.Size;

public class ResumeRequest {

    @Size(max = 500)
    private String objective;

    @Size(max = 2000)
    private String education;

    @Size(max = 2000)
    private String experience;

    @Size(max = 2000)
    private String projects;

    @Size(max = 2000)
    private String certifications;

    @Size(max = 2000)
    private String skills;

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
