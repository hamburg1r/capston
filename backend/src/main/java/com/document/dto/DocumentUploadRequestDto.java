package com.document.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DocumentUploadRequestDto {

    @NotBlank(message = "File name cannot be empty")
    @Size(max = 255, message = "File name is too long")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Filename contains invalid characters")
    private String fileName;

    @NotBlank(message = "File type cannot be empty")
    @Pattern(regexp = "^(application|image|text)/[a-z0-9.-]+$")
    private String fileType;

    @NotBlank
    @Min(1)
    @Max(104857600) //100mb
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
