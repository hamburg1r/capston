package com.document.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private int statusCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(int statusCode, String message, LocalDateTime timestamp, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
