package com.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DocumentUploadRequestDto {

    @NotBlank(message = "File name cannot be empty")
    @Size(max = 255, message = "File name is too long")
    private String fileName;

    @NotBlank(message = "File type cannot be empty")
    private String fileType;

    @NotBlank(message = "File size cannot be empty")
//    @Pattern(
//        regexp = "^[0-9]+$",
//        message = "File size must be numeric in bytes"
//    )
    private String fileSize;

    public DocumentUploadRequestDto() {}

    public DocumentUploadRequestDto(String fileName, String fileType, String fileSize) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
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

    public String getFileSize() {
        return fileSize;
    }
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
