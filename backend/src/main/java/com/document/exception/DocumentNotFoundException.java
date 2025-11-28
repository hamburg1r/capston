package com.document.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String documentId) {
        super("Document not found: " + documentId);
    }
    
    public DocumentNotFoundException(String documentId, String userId) {
        super("Document not found: " + documentId + " for user: " + userId);
    }
}

