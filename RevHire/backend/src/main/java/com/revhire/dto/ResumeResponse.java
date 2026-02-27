package com.revhire.dto;

public class ResumeResponse {

    private String objective;
    private String education;
    private String experience;
    private String projects;
    private String certifications;
    private String skills;
    private String uploadedFileName;
    private String uploadedFileType;
    private Long uploadedFileSize;
    private String uploadedFileReference;

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

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getUploadedFileType() {
        return uploadedFileType;
    }

    public void setUploadedFileType(String uploadedFileType) {
        this.uploadedFileType = uploadedFileType;
    }

    public Long getUploadedFileSize() {
        return uploadedFileSize;
    }

    public void setUploadedFileSize(Long uploadedFileSize) {
        this.uploadedFileSize = uploadedFileSize;
    }

    public String getUploadedFileReference() {
        return uploadedFileReference;
    }

    public void setUploadedFileReference(String uploadedFileReference) {
        this.uploadedFileReference = uploadedFileReference;
    }
}
