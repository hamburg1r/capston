package com.document.controller;

import com.document.dto.DocumentResponseDTO;
import com.document.dto.DocumentUploadRequestDto;
import com.document.exception.DocumentNotFoundException;
import com.document.model.DocumentModel;
import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import com.document.service.SNSService;
import com.document.service.SQSService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentServiceImp documentService;
    private final S3Service s3Service;
    private final SNSService snsService;
    private final SQSService sqsService;

    public DocumentController(DocumentServiceImp documentService,
                              S3Service s3Service,
                              SNSService snsService,
                              SQSService sqsService) {
        this.documentService = documentService;
        this.s3Service = s3Service;
        this.snsService = snsService;
        this.sqsService = sqsService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@RequestBody @Valid DocumentUploadRequestDto req) {
        log.info(" Request received to generate upload URL for file: {}", req.getFileName());
        try {
            String userId = getUserId();
            DocumentModel document = documentService.createDocument(
                    userId, req.getFileName(), req.getFileType(), req.getFileSize()
            );

            String s3Key = userId + "/" + document.getDocumentId() + "/" + req.getFileName();
            String url = s3Service.generatePresignedUrl(s3Key, req.getFileType());

            log.info(" Presigned upload URL generated for docId={} by user={}", document.getDocumentId(), userId);

            DocumentResponseDTO response = new DocumentResponseDTO();
            response.setDocumentId(document.getDocumentId());
            response.setUploadUrl(url);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error(" Failed to generate upload URL: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload URL create failed: " + ex.getMessage());
        }
    }

    @PostMapping("/{documentId}/complete")
    public ResponseEntity<?> markUploadComplete(@PathVariable String documentId,
                                                @RequestBody DocumentModel req) {
        log.info(" Marking upload complete for docId={}", documentId);
        try {
            String userId = getUserId();
            documentService.markUploadCompleted(documentId, userId,
                    req.getFileName(), req.getFileType(), req.getFileSize());

            log.info(" Successfully marked documentId={} upload completed", documentId);
            return ResponseEntity.ok(Map.of("message", "Upload Completed"));

        } catch (DocumentNotFoundException e) {
            log.warn(" Document not found while completing upload: {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found!");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDocuments() {
        String userId = getUserId();
        log.info(" Fetching documents for user={}", userId);
        List<DocumentModel> list = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> download(@PathVariable String documentId) {
        log.info(" Request to download documentId={}", documentId);
        try {
            String userId = getUserId();
            String url = documentService.generateDownloadUrl(documentId, userId);

            log.info(" Download URL generated for documentId={}", documentId);
         
            return ResponseEntity.ok(Map.of("downloadUrl", url));

        } catch (DocumentNotFoundException e) {
            log.error(" Download failed: Document not found {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found");
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        log.info(" Delete request for documentId={}", documentId);
        try {
            String userId = getUserId();
            documentService.deleteDocument(documentId, userId);

            log.info(" Successfully deleted documentId={}", documentId);
            return ResponseEntity.noContent().build();

        } catch (DocumentNotFoundException e) {
            log.warn(" Cannot delete => Document Not Found: {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delete Failed: Not Found");
        } catch (Exception e) {
            log.error(" S3 Delete failed due to permission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("S3 Permission Denied: " + e.getMessage());
        }
    }

    private String getUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        log.debug(" Extracted UserId: {}", userId);
        return userId;
    }
}
