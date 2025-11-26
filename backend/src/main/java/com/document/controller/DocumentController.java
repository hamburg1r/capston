package com.document.controller;

import com.document.model.DocumentModel;

import com.document.service.DocumentServiceImp;
import com.document.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentServiceImp documentService;
    private final S3Service s3Service;

    public DocumentController(DocumentServiceImp documentService, S3Service s3Service) {
        this.documentService = documentService;
        this.s3Service = s3Service;
    }

    @PostMapping("/presigned-url")
    public Map<String, Object> generatePresignedUrl(@RequestBody Map<String, String> req) {

        String fileName = req.get("fileName");
        String fileType = req.get("fileType");
        String fileSize = req.get("fileSize");

        String userId = getUserId();

 
        DocumentModel document = documentService.createDocument(userId, fileName, fileType,fileSize);


        String s3Key = userId + "/" + document.getDocumentId() + "/" + fileName;

  
        String uploadUrl = s3Service.generatePresignedUrl(s3Key, fileType);

        Map<String, Object> map = new HashMap<>();
        map.put("uploadUrl", uploadUrl);
        map.put("documentId", document.getDocumentId());

        return map;
    }


    @PostMapping("/{documentId}/complete")
    public String markUploadComplete(@PathVariable String documentId, @RequestBody Map<String, Object> req) {

        String userId = getUserId();

        String fileSize = req.get("fileSize").toString();
        String fileType = req.get("fileType").toString();
        String fileName = req.get("fileName").toString();


        documentService.markUploadCompleted(documentId, userId, fileName, fileType, fileSize);
      

        return "Document upload completed";
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
