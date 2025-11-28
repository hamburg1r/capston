package com.document.exception;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String fileType) {
        super("File type not allowed: " + fileType);
    }
}

