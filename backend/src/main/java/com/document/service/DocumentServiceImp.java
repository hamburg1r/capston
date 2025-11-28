package com.document.service;

import com.document.exception.DocumentNotFoundException;
import com.document.model.DocumentModel;
import com.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImp implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImp.class);

    private final DocumentRepository repo;
    private final S3Service s3Service;

    public DocumentServiceImp(DocumentRepository repo, S3Service s3Service) {
        this.repo = repo;
        this.s3Service = s3Service;
    }

    public DocumentModel createDocument(String userId, String fileName, String fileType, String fileSize) {
        log.info(" Creating document for userId={}, fileName={}", userId, fileName);
        try {
            DocumentModel doc = new DocumentModel();
            doc.setDocumentId(UUID.randomUUID().toString());
            doc.setUserId(userId);
            doc.setFileName(fileName);
            doc.setFileType(fileType);
            doc.setFileSize(fileSize);
            doc.setStatus("UPLOADED");
            doc.setUploadDate(LocalDateTime.now().toString());

            repo.save(doc);

            log.info(" Document created successfully: docId={}", doc.getDocumentId());
            return doc;

        } catch (Exception ex) {
            log.error(" Failed to create document: {}", ex.getMessage());
            throw new RuntimeException("Failed to create document: " + ex.getMessage());
        }
    }

    public void updateDocument(DocumentModel doc) {
        log.info(" Updating document docId={}", doc.getDocumentId());
        try {
            repo.save(doc);
            log.info(" Document updated successfully docId={}", doc.getDocumentId());
        } catch (Exception ex) {
            log.error(" Failed to update document: {}", ex.getMessage());
            throw new RuntimeException("Failed to update document: " + ex.getMessage());
        }
    }

    public DocumentModel getById(String docId, String userId) {
        log.info(" Finding document docId={} for userId={}", docId, userId);
        DocumentModel doc = repo.findByDocumentIdAndUserId(docId, userId);

        if (doc == null) {
            log.warn(" Document not found: {}", docId);
            throw new DocumentNotFoundException("Document not found");
        }

        log.info(" Document found for docId={}", docId);
        return doc;
    }

    public List<DocumentModel> getUserDocuments(String userId) {
        log.info(" Fetching document list for user={}", userId);
        try {
            return repo.findByUserId(userId);
        } catch (Exception ex) {
            log.error(" Failed to fetch user documents: {}", ex.getMessage());
            throw new RuntimeException("Failed to fetch user documents: " + ex.getMessage());
        }
    }

    public void markUploadCompleted(String documentId, String userId,
                                    String fileName, String fileType, String fileSize) {
        log.info(" Marking upload complete for docId={} by user={}", documentId, userId);
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);

            if (doc == null) {
                log.warn(" Document not found while completing upload: {}", documentId);
                throw new DocumentNotFoundException("Document not found while completing upload");
            }

            String s3Key = userId + "/" + documentId + "/" + fileName;

            doc.setFileName(fileName);
            doc.setFileType(fileType);
            doc.setFileSize(fileSize);
            doc.setS3Key(s3Key);
            doc.setStatus("COMPLETED");

            repo.save(doc);

            log.info(" Upload completed & document updated: {}", documentId);

        } catch (Exception ex) {
            log.error(" Failed to complete upload: {}", ex.getMessage());
            throw ex;
        }
    }

    public String generateDownloadUrl(String documentId, String userId) {
        log.info(" Generating download URL for docId={} user={}", documentId, userId);
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
            if (doc == null) {
                log.warn(" Document not found while generating download URL: {}", documentId);
                throw new DocumentNotFoundException("Document not found for download");
            }

            String url = s3Service.generateDownloadUrl(doc.getS3Key());
            log.info(" Download URL generated for docId={}", documentId);

            return url;

        } catch (Exception ex) {
            log.error(" Failed to generate download URL: {}", ex.getMessage());
            throw ex;
        }
    }

    public void deleteDocument(String documentId, String userId) {
        log.info(" Deleting document docId={}", documentId);
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
            if (doc == null) {
                log.warn(" Document not found to delete: {}", documentId);
                throw new DocumentNotFoundException("Document not found to delete");
            }

            if (doc.getS3Key() != null) {
                s3Service.deleteFile(doc.getS3Key());
                log.info(" S3 file deleted: {}", doc.getS3Key());
            }

            repo.delete(doc);
            log.info(" Document deleted from DB docId={}", documentId);

        } catch (Exception ex) {
            log.error(" Failed to delete document: {}", ex.getMessage());
            throw ex;
        }
    }
}
