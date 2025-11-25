package com.document.model;

import java.time.LocalDateTime;

@DynamoDbBean
public class DocumentItem {
    
    private String documentId;
    private String userId;
    private String s3Key;
    private String fileName;
    private String fileType;
    private String status;
    private LocalDateTime uploadDate;
    // private Map<String, Object> processingDetails;
    @DynamoDbPartitionKey
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    @DynamoDbSortKey
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return uploadDate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.uploadDate = createdAt;
    }

    // public Map<String, Object> getProcessingDetails() {
    //     return processingDetails;
    // }

    // public void setProcessingDetails(Map<String, Object> processingDetails) {
    //     this.processingDetails = processingDetails;
    // }
}
