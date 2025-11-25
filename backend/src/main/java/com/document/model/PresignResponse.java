package com.document.model;

public class PresignResponse {
    private String uploadUrl;
    private String documentId;
    private String s3Key;

    public PresignResponse() {
    }

    public PresignResponse(String uploadUrl, String documentId, String s3Key) {
        this.uploadUrl = uploadUrl;
        this.documentId = documentId;
        this.s3Key = s3Key;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
}
