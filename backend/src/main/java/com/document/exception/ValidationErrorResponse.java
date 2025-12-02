package com.document.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, String> errors;

    public ValidationErrorResponse(int statusCode, String message,
                                   LocalDateTime timestamp, String path,
                                   Map<String, String> errors) {
        super(statusCode, message, timestamp, path);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
