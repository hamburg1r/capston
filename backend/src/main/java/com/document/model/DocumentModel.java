package com.document.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Documents")
public class DocumentModel {

    private String documentId;
    private String userId;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String s3Key;
    private String status;
    private String uploadDate; 

   

    @DynamoDBHashKey(attributeName = "documentId")
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @DynamoDBRangeKey(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDBAttribute(attributeName = "fileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @DynamoDBAttribute(attributeName = "fileType")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @DynamoDBAttribute(attributeName = "fileSize")
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @DynamoDBAttribute(attributeName = "s3Key")
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDBAttribute(attributeName = "uploadDate")
    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

	@Override
	public String toString() {
		return "DocumentModel [documentId=" + documentId + ", userId=" + userId + ", fileName=" + fileName
				+ ", fileType=" + fileType + ", fileSize=" + fileSize + ", s3Key=" + s3Key + ", status=" + status
				+ ", uploadDate=" + uploadDate + "]";
	}
    
}
