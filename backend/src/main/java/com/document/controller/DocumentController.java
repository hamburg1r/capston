package com.document.controller;

import com.document.dto.DocumentResponseDTO;
import com.document.dto.DocumentUploadRequestDto;
import com.document.model.DocumentModel;
import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import com.document.service.SNSService;
import com.document.service.SQSService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentServiceImp documentService;
    private final S3Service s3Service;
    private final SNSService snsService;
    private final SQSService sqsService;

    public DocumentController(
            DocumentServiceImp documentService,
            S3Service s3Service,
            SNSService snsService,
            SQSService sqsService) {
        this.documentService = documentService;
        this.s3Service = s3Service;
        this.snsService = snsService;
        this.sqsService = sqsService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<DocumentResponseDTO> generatePresignedUrl(@RequestBody DocumentUploadRequestDto req) {

        String fileName = req.getFileName();
        String fileType = req.getFileType();
        String fileSize = req.getFileSize();

        String userId = getUserId();

        DocumentModel document = documentService.createDocument(userId, fileName, fileType, fileSize);
        String s3Key = userId + "/" + document.getDocumentId() + "/" + fileName;

        String uploadUrl = s3Service.generatePresignedUrl(s3Key, fileType);

        DocumentResponseDTO documentResponseDTO = new DocumentResponseDTO();
        documentResponseDTO.setUploadUrl(uploadUrl);
        documentResponseDTO.setDocumentId(document.getDocumentId());

        return ResponseEntity.ok(documentResponseDTO);
    }

    @PostMapping("/{documentId}/complete")
    public ResponseEntity<Map<String, String>> markUploadComplete(
            @PathVariable String documentId, @RequestBody DocumentModel req) {

        String userId = getUserId();

        documentService.markUploadCompleted(
                documentId, userId, req.getFileName(), req.getFileType(), req.getFileSize()
        );

        String snsMessageId = snsService.publishDocumentUploadNotification(
                documentId, userId, req.getFileName(), req.getFileType(), req.getFileSize()
        );

        String s3Key = userId + "/" + documentId + "/" + req.getFileName();
        String sqsMessageId = sqsService.sendDocumentProcessingTask(
                documentId, userId, req.getFileName(), req.getFileType(), s3Key
        );

        sqsService.sendMetadataExtractionTask(documentId, userId, s3Key, req.getFileType());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Document upload completed");
        response.put("snsMessageId", snsMessageId);
        response.put("sqsMessageId", sqsMessageId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentModel>> getUserDocuments() {
        String userId = getUserId();
        List<DocumentModel> list = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<String> download(@PathVariable String documentId) {
        String userId = getUserId();
        String url = documentService.generateDownloadUrl(documentId, userId);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        String userId = getUserId();
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build();
    }

    private String getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString();
    }
}
