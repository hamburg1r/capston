package com.document.controller;

import com.document.dto.DocumentResponseDTO;
import com.document.dto.DocumentUploadRequestDto;
import com.document.model.DocumentModel;
import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import com.document.service.SNSService;
import com.document.service.SQSService;
import com.document.validator.FileValidator;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final FileValidator fileValidator;

    public DocumentController(DocumentServiceImp documentService,
                              S3Service s3Service,
                              SNSService snsService,
                              SQSService sqsService,
                              FileValidator fileValidator) {
        this.documentService = documentService;
        this.s3Service = s3Service;
        this.snsService = snsService;
        this.sqsService = sqsService;
        this.fileValidator = fileValidator;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@Valid @RequestBody DocumentUploadRequestDto req) {

        String userId = getUserId();
        log.info("Generate URL for file: {} by user={}", req.getFileName(), userId);
        
        fileValidator.validateFile(
                req.getFileName(),
                req.getFileType(),
                Long.parseLong(req.getFileSize())
        );

        DocumentModel document = documentService.createDocument(
                userId, req.getFileName(), req.getFileType(), req.getFileSize()
        );

        String s3Key = userId + "/" + document.getDocumentId() + "/" + req.getFileName();
        document.setS3Key(s3Key);
        documentService.updateDocument(document);

        String url = s3Service.generatePresignedUrl(s3Key, req.getFileType());

        DocumentResponseDTO response = new DocumentResponseDTO();
        response.setDocumentId(document.getDocumentId());
        response.setUploadUrl(url);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{documentId}/complete")
    public ResponseEntity<?> markUploadComplete(@PathVariable String documentId,
                                                @RequestBody DocumentModel req) {

        String userId = getUserId();
        documentService.markUploadCompleted(
                documentId, userId,
                req.getFileName(), req.getFileType(), req.getFileSize()
        );

        snsService.publishDocumentUploadNotification(
                documentId, userId, req.getFileName(), req.getFileType(), req.getFileSize()
        );

        String s3Key = userId + "/" + documentId + "/" + req.getFileName();

        sqsService.sendDocumentProcessingTask(documentId, userId, req.getFileName(), req.getFileType(), s3Key);
        sqsService.sendMetadataExtractionTask(documentId, userId, s3Key, req.getFileType());

        return ResponseEntity.ok(Map.of(
                "message", "Upload Completed",
                "documentId", documentId,
                "status", "PROCESSING"
        ));
    }

    @GetMapping
    public ResponseEntity<?> getUserDocuments() {
        String userId = getUserId();
        List<DocumentModel> list = documentService.getUserDocuments(userId);
        for(DocumentModel m : list) {
        	System.out.println(m);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String documentId) {
        String userId = getUserId();
        return ResponseEntity.ok(documentService.getById(documentId, userId));
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> download(@PathVariable String documentId) {
        String userId = getUserId();
        String url = documentService.generateDownloadUrl(documentId, userId);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        String userId = getUserId();
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/reprocess")
    public ResponseEntity<?> reprocessDocument(@PathVariable String documentId) {
        String userId = getUserId();
        DocumentModel doc = documentService.getById(documentId, userId);

        sqsService.sendDocumentProcessingTask(
                documentId, userId, doc.getFileName(), doc.getFileType(), doc.getS3Key()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Document queued for reprocessing",
                "documentId", documentId
        ));
    }

    private String getUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        log.debug("Extracted UserId: {}", userId);
        return userId;
    }
}
