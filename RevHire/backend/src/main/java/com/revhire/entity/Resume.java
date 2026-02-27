package com.revhire.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 500)
    private String objective;

    @Column(length = 2000)
    private String educationCsv;

    @Column(length = 2000)
    private String experienceCsv;

    @Column(length = 2000)
    private String projectsCsv;

    @Column(length = 2000)
    private String certificationsCsv;

    @Column(length = 2000)
    private String skills;

    @Column(length = 255)
    private String uploadedFileName;

    @Column(length = 100)
    private String uploadedFileType;

    private Long uploadedFileSize;

    @Column(length = 100)
    private String uploadedFileReference;

    @Lob
    @Column(name = "uploaded_file_data", columnDefinition = "LONGBLOB")
    private byte[] uploadedFileData;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getEducationCsv() {
        return educationCsv;
    }

    public void setEducationCsv(String educationCsv) {
        this.educationCsv = educationCsv;
    }

    public String getExperienceCsv() {
        return experienceCsv;
    }

    public void setExperienceCsv(String experienceCsv) {
        this.experienceCsv = experienceCsv;
    }

    public String getProjectsCsv() {
        return projectsCsv;
    }

    public void setProjectsCsv(String projectsCsv) {
        this.projectsCsv = projectsCsv;
    }

    public String getCertificationsCsv() {
        return certificationsCsv;
    }

    public void setCertificationsCsv(String certificationsCsv) {
        this.certificationsCsv = certificationsCsv;
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

    public byte[] getUploadedFileData() {
        return uploadedFileData;
    }

    public void setUploadedFileData(byte[] uploadedFileData) {
        this.uploadedFileData = uploadedFileData;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
