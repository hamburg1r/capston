package com.document.controller;

import com.document.model.DocumentModel;

import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import com.document.service.SNSService;
import com.document.service.SQSService;

import jakarta.servlet.http.HttpServletRequest;
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
    public Map<String, Object> generatePresignedUrl(@RequestBody Map<String, String> req) {

        String fileName = req.get("fileName");
        String fileType = req.get("fileType");
        String fileSize = req.get("fileSize");

        String userId = getUserId();

        DocumentModel document = documentService.createDocument(userId, fileName, fileType, fileSize);

        String s3Key = userId + "/" + document.getDocumentId() + "/" + fileName;

        String uploadUrl = s3Service.generatePresignedUrl(s3Key, fileType);

        Map<String, Object> map = new HashMap<>();
        map.put("uploadUrl", uploadUrl);
        map.put("documentId", document.getDocumentId());

        return map;
    }

    @PostMapping("/{documentId}/complete")
    public Map<String, String> markUploadComplete(@PathVariable String documentId, @RequestBody Map<String, Object> req) {

        String userId = getUserId();

        String fileSize = req.get("fileSize").toString();
        String fileType = req.get("fileType").toString();
        String fileName = req.get("fileName").toString();

        documentService.markUploadCompleted(documentId, userId, fileName, fileType, fileSize);

        // Publish SNS notification for document upload
        String snsMessageId = snsService.publishDocumentUploadNotification(
                documentId, userId, fileName, fileType, fileSize
        );

        // Send processing task to SQS
        String s3Key = userId + "/" + documentId + "/" + fileName;
        String sqsMessageId = sqsService.sendDocumentProcessingTask(
                documentId, userId, fileName, fileType, s3Key
        );

        // Send metadata extraction task to SQS
        sqsService.sendMetadataExtractionTask(documentId, userId, s3Key, fileType);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Document upload completed");
        response.put("snsMessageId", snsMessageId);
        response.put("sqsMessageId", sqsMessageId);

        return response;
    }

    @GetMapping
    public List<DocumentModel> getUserDocuments() {
        String userId = getUserId();
        return documentService.getUserDocuments(userId);
    }

    private String getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString();
    }
}
