package com.document.validator;

import com.document.exception.FileSizeExceededException;
import com.document.exception.InvalidFileTypeException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FileValidator {
    
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png",
        "text/plain",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long MIN_FILE_SIZE = 1; // 1 byte
    
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
        ".exe", ".bat", ".cmd", ".com", ".sh", ".scr", ".vbs", ".js"
    );
    
    // file -> type, size, name
    public void validateFile(String fileName, String fileType, long fileSize) {
        validateFileType(fileType);
        validateFileSize(fileSize);
        validateFileName(fileName);
    }
    
    // type
    private void validateFileType(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new InvalidFileTypeException("File type cannot be empty");
        }
        
        if (!ALLOWED_TYPES.contains(fileType.toLowerCase())) {
            throw new InvalidFileTypeException(
                "File type '" + fileType + "' is not allowed. Allowed types: " + 
                String.join(", ", ALLOWED_TYPES)
            );
        }
    }
    
    // size
    private void validateFileSize(long fileSize) {
        if (fileSize < MIN_FILE_SIZE) {
            throw new FileSizeExceededException(
                "File size must be at least " + MIN_FILE_SIZE + " byte"
            );
        }
        
        if (fileSize > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(
                "File size exceeds maximum limit of " + 
                (MAX_FILE_SIZE / 1024 / 1024) + "MB"
            );
        }
    }
    
    // name
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileTypeException("File name cannot be empty");
        }
        
        //  path traversal attempts
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new InvalidFileTypeException(
                "File name contains invalid characters (path traversal attempt detected)"
            );
        }
        
        //  dangerous extensions
        String lowerFileName = fileName.toLowerCase();
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                throw new InvalidFileTypeException(
                    "File extension '" + ext + "' is not allowed for security reasons"
                );
            }
        }
        
        //  null bytes
        if (fileName.contains("\0")) {
            throw new InvalidFileTypeException(
                "File name contains null bytes"
            );
        }
        
        // length
        if (fileName.length() > 255) {
            throw new InvalidFileTypeException(
                "File name is too long (max 255 characters)"
            );
        }
    }
    
    // sanitize 
    public String sanitizeFileName(String fileName) {
        // Remove any characters that are not alphanumeric, dots, hyphens, or underscores
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    
    public boolean isAllowedFileType(String fileType) {
        return ALLOWED_TYPES.contains(fileType.toLowerCase());
    }
    
    
    public String getHumanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
