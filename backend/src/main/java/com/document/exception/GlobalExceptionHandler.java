//package com.document.exception;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.context.request.WebRequest;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(DocumentNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleDocumentNotFound(
//            DocumentNotFoundException ex, WebRequest request) {
//        
//        ErrorResponse error = new ErrorResponse(
//                HttpStatus.NOT_FOUND.value(),
//                ex.getMessage(),
//                LocalDateTime.now(),
//                request.getDescription(false)
//        );
//        
//        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//    }
//
//
//    @ExceptionHandler(InvalidFileTypeException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidFileType(
//            InvalidFileTypeException ex, WebRequest request) {
//        
//        ErrorResponse error = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                ex.getMessage(),
//                LocalDateTime.now(),
//                request.getDescription(false)
//        );
//        
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(FileSizeExceededException.class)
//    public ResponseEntity<ErrorResponse> handleFileSizeExceeded(
//            FileSizeExceededException ex, WebRequest request) {
//        
//        ErrorResponse error = new ErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                ex.getMessage(),
//                LocalDateTime.now(),
//                request.getDescription(false)
//        );
//        
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
//
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
//            MethodArgumentNotValidException ex, WebRequest request) {
//        
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ValidationErrorResponse response = new ValidationErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "Validation failed",
//                LocalDateTime.now(),
//                request.getDescription(false),
//                errors
//        );
//        
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGlobalException(
//            Exception ex, WebRequest request) {
//        
//        ErrorResponse error = new ErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "An unexpected error occurred",
//                LocalDateTime.now(),
//                request.getDescription(false)
//        );
//        
//        // Log the actual exception for debugging (use proper logger in production)
//        System.err.println("Unhandled exception: " + ex.getMessage());
//        ex.printStackTrace();
//        
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
//
//@Data
//@AllArgsConstructor
//class ErrorResponse {
//    private int statusCode;
//    private String message;
//    private LocalDateTime timestamp;
//    private String path;
//}
//
//@Data
//class ValidationErrorResponse extends ErrorResponse {
//    private Map<String, String> errors;
//    
//    public ValidationErrorResponse(int statusCode, String message, 
//                                   LocalDateTime timestamp, String path,
//                                   Map<String, String> errors) {
//        super(statusCode, message, timestamp, path);
//        this.errors = errors;
//    }
//}
