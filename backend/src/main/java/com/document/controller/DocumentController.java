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
@CrossOrigin(origins = "${app.frontend.url}")
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
        log.info("Request received to generate upload URL for file: {}", req.getFileName());
        try {
            String userId = getUserId();
            DocumentModel document = documentService.createDocument(
                    userId, req.getFileName(), req.getFileType(), req.getFileSize()
            );

            String s3Key = userId + "/" + document.getDocumentId() + "/" + req.getFileName();
            document.setS3Key(s3Key);
            documentService.updateDocument(document);
            
            String url = s3Service.generatePresignedUrl(s3Key, req.getFileType());

            log.info("Presigned upload URL generated for docId={} by user={}", document.getDocumentId(), userId);

            DocumentResponseDTO response = new DocumentResponseDTO();
            response.setDocumentId(document.getDocumentId());
            response.setUploadUrl(url);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Failed to generate upload URL: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload URL creation failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/{documentId}/complete")
    public ResponseEntity<?> markUploadComplete(@PathVariable String documentId,
                                                @RequestBody DocumentModel req) {
        log.info("Marking upload complete for docId={}", documentId);
        try {
            String userId = getUserId();
            documentService.markUploadCompleted(documentId, userId,
                    req.getFileName(), req.getFileType(), req.getFileSize());

            log.info("Successfully marked documentId={} upload completed", documentId);

            // Send SNS notification about upload
            try {
                snsService.publishDocumentUploadNotification(
                        documentId, userId, req.getFileName(), 
                        req.getFileType(), req.getFileSize()
                );
                log.info("SNS notification sent for document upload: {}", documentId);
            } catch (Exception e) {
                log.warn("Failed to send SNS notification: {}", e.getMessage());
            }

            // Send SQS message for processing
            try {
                String s3Key = userId + "/" + documentId + "/" + req.getFileName();
                sqsService.sendDocumentProcessingTask(
                        documentId, userId, req.getFileName(), 
                        req.getFileType(), s3Key
                );
                log.info("SQS processing task sent for document: {}", documentId);
                
                // Also send metadata extraction task
                sqsService.sendMetadataExtractionTask(
                        documentId, userId, s3Key, req.getFileType()
                );
                log.info("SQS metadata extraction task sent for document: {}", documentId);
            } catch (Exception e) {
                log.error("Failed to send SQS task: {}", e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Upload Completed",
                    "documentId", documentId,
                    "status", "PROCESSING"
            ));

        } catch (DocumentNotFoundException e) {
            log.warn("Document not found while completing upload: {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document Not Found"));
        } catch (Exception e) {
            log.error("Failed to complete upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete upload: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDocuments() {
        String userId = getUserId();
        log.info("Fetching documents for user={}", userId);
        try {
            List<DocumentModel> list = documentService.getUserDocuments(userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to fetch documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch documents"));
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String documentId) {
        log.info("Fetching document details for docId={}", documentId);
        try {
            String userId = getUserId();
            DocumentModel doc = documentService.getById(documentId, userId);
            return ResponseEntity.ok(doc);
        } catch (DocumentNotFoundException e) {
            log.error("Document not found: {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document Not Found"));
        } catch (Exception e) {
            log.error("Failed to fetch document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch document"));
        }
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> download(@PathVariable String documentId) {
        log.info("Request to download documentId={}", documentId);
        try {
            String userId = getUserId();
            String url = documentService.generateDownloadUrl(documentId, userId);

            log.info("Download URL generated for documentId={}", documentId);
            return ResponseEntity.ok(Map.of("downloadUrl", url));

        } catch (DocumentNotFoundException e) {
            log.error("Download failed: Document not found {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document Not Found"));
        } catch (Exception e) {
            log.error("Failed to generate download URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate download URL"));
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        log.info("Delete request for documentId={}", documentId);
        try {
            String userId = getUserId();
            documentService.deleteDocument(documentId, userId);

            log.info("Successfully deleted documentId={}", documentId);
            return ResponseEntity.noContent().build();

        } catch (DocumentNotFoundException e) {
            log.warn("Cannot delete => Document Not Found: {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Delete Failed: Not Found"));
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Delete Failed: " + e.getMessage()));
        }
    }

    @PostMapping("/{documentId}/reprocess")
    public ResponseEntity<?> reprocessDocument(@PathVariable String documentId) {
        log.info("Reprocess request for documentId={}", documentId);
        try {
            String userId = getUserId();
            DocumentModel doc = documentService.getById(documentId, userId);

            // Send to SQS for reprocessing
            sqsService.sendDocumentProcessingTask(
                    documentId, userId, doc.getFileName(), 
                    doc.getFileType(), doc.getS3Key()
            );

            log.info("Reprocessing task sent for documentId={}", documentId);
            return ResponseEntity.ok(Map.of(
                    "message", "Document queued for reprocessing",
                    "documentId", documentId
            ));

        } catch (DocumentNotFoundException e) {
            log.error("Reprocess failed: Document not found {}", documentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document Not Found"));
        } catch (Exception e) {
            log.error("Failed to queue reprocessing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to queue reprocessing"));
        }
    }

    private String getUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        log.debug("Extracted UserId: {}", userId);
        return userId;
    }
}