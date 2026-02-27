package com.revhire.dto;

public class ResumeFilePayload {

    private final String fileName;
    private final String fileType;
    private final byte[] fileData;

    public ResumeFilePayload(String fileName, String fileType, byte[] fileData) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
