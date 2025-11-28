package com.document.controller;

import com.document.dto.DocumentResponseDTO;
import com.document.dto.DocumentUploadRequestDto;
import com.document.exception.DocumentNotFoundException;
import com.document.model.DocumentModel;
import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import com.document.service.SNSService;
import com.document.service.SQSService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

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
    public ResponseEntity<?> generatePresignedUrl(@RequestBody    @Valid DocumentUploadRequestDto req) {
        try {
            String userId = getUserId();
            DocumentModel document = documentService.createDocument(
                    userId, req.getFileName(), req.getFileType(), req.getFileSize()
            );

            String s3Key = userId + "/" + document.getDocumentId() + "/" + req.getFileName();
            String url = s3Service.generatePresignedUrl(s3Key, req.getFileType());

            url = url.replace("http://", "https://"); // force HTTPS

            DocumentResponseDTO response = new DocumentResponseDTO();
            response.setDocumentId(document.getDocumentId());
            response.setUploadUrl(url);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload URL create failed: " + ex.getMessage());
        }
    }

    @PostMapping("/{documentId}/complete")
    public ResponseEntity<?> markUploadComplete(@PathVariable String documentId,
                                                @RequestBody DocumentModel req) {
        try {
            String userId = getUserId();
            documentService.markUploadCompleted(documentId, userId, req.getFileName(),
                    req.getFileType(), req.getFileSize());

            return ResponseEntity.ok(Map.of("message", "Upload Completed"));

        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found!");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDocuments() {
        String userId = getUserId();
        List<DocumentModel> list = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> download(@PathVariable String documentId) {
        try {
            String userId = getUserId();
            String url = documentService.generateDownloadUrl(documentId, userId);

           // url = url.replace("http://", "https://"); // enforce SSL

            return ResponseEntity.ok(Map.of("downloadUrl", url));

        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document Not Found");
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        try {
            String userId = getUserId();
            documentService.deleteDocument(documentId, userId);
            return ResponseEntity.noContent().build();

        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delete Failed: Not Found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("S3 Permission Denied: " + e.getMessage());
        }
    }

    private String getUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }
}
