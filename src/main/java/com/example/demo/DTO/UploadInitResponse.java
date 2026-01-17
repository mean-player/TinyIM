package com.example.demo.DTO;

public class UploadInitResponse {
    private String uploadId;
    private Boolean newCreated;


    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Boolean getNewCreated() {
        return newCreated;
    }

    public void setNewCreated(Boolean newCreated) {
        this.newCreated = newCreated;
    }
}
