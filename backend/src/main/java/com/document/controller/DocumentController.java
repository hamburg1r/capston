package com.document.controller;

import com.document.dto.DocumentResponseDTO;
import com.document.dto.DocumentUploadRequestDto;
import com.document.model.DocumentModel;

import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;

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

    public DocumentController(DocumentServiceImp documentService, S3Service s3Service) {
        this.documentService = documentService;
        this.s3Service = s3Service;
    }

    @PostMapping("/presigned-url")
    public DocumentResponseDTO generatePresignedUrl(@RequestBody Map<String, String> req) {

        String fileName = req.get("fileName");
        String fileType = req.get("fileType");
        String fileSize = req.get("fileSize");

        String userId = getUserId();

 
        DocumentModel document = documentService.createDocument(userId, fileName, fileType,fileSize);


        String s3Key = userId + "/" + document.getDocumentId() + "/" + fileName;

  
        String uploadUrl = s3Service.generatePresignedUrl(s3Key, fileType);
         DocumentResponseDTO documentResponseDTO= new DocumentResponseDTO();
       
         documentResponseDTO.setUploadUrl(uploadUrl);
         documentResponseDTO.setDocumentId(document.getDocumentId());

        return documentResponseDTO;
    }


    @PostMapping("/{documentId}/complete")
    public String markUploadComplete(@PathVariable String documentId, @RequestBody DocumentUploadRequestDto req) {

        String userId = getUserId();

        String fileSize = req.getFileSize();
        String fileType = req.getFileType();
        String fileName = req.getFileName();


        documentService.markUploadCompleted(documentId, userId, fileName, fileType, fileSize);
      

        return "Document upload completed";
    }


    @GetMapping
    public List<DocumentModel> getUserDocuments() {
        String userId = getUserId();
        System.out.println(userId);
        return documentService.getUserDocuments(userId);
    }
    @GetMapping("/download/{documentId}")
    public String download(@PathVariable String documentId) {
        String userId = getUserId();
        return documentService.generateDownloadUrl(documentId, userId);
    }


    private String getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.toString();
    }
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        String userId = getUserId(); // jo tum already use kar rahe ho JWT se
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build(); // 204
    }
}
