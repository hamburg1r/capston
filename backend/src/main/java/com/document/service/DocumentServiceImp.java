package com.document.service;

import com.document.exception.DocumentNotFoundException;
import com.document.model.DocumentModel;
import com.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImp implements DocumentService {

    private final DocumentRepository repo;
    private final S3Service s3Service;

    public DocumentServiceImp(DocumentRepository repo, S3Service s3Service) {
        this.repo = repo;
        this.s3Service = s3Service;
    }

    public DocumentModel createDocument(String userId, String fileName, String fileType, String fileSize) {
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
            return doc;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create document: " + ex.getMessage());
        }
    }

    public void updateDocument(DocumentModel doc) {
        try {
            repo.save(doc);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update document: " + ex.getMessage());
        }
    }

    public DocumentModel getById(String docId, String userId) {
        DocumentModel doc = repo.findByDocumentIdAndUserId(docId, userId);
        if (doc == null) {
            throw new DocumentNotFoundException("Document not found");
        }
        return doc;
    }

    public List<DocumentModel> getUserDocuments(String userId) {
        try {
            return repo.findByUserId(userId);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch user documents: " + ex.getMessage());
        }
    }

    public void markUploadCompleted(String documentId, String userId,
                                    String fileName, String fileType, String fileSize) {
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);

            if (doc == null) {
                throw new DocumentNotFoundException("Document not found while completing upload");
            }

            String s3Key = userId + "/" + documentId + "/" + fileName;

            doc.setFileName(fileName);
            doc.setFileType(fileType);
            doc.setFileSize(fileSize);
            doc.setS3Key(s3Key);
            doc.setStatus("COMPLETED");

            repo.save(doc);
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to complete upload: " + ex.getMessage());
        }
    }

    public String generateDownloadUrl(String documentId, String userId) {
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
            if (doc == null) {
                throw new DocumentNotFoundException("Document not found for download");
            }

            return s3Service.generateDownloadUrl(doc.getS3Key());
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate download URL: " + ex.getMessage());
        }
    }

    public void deleteDocument(String documentId, String userId) {
        try {
            DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
            if (doc == null) {
                throw new DocumentNotFoundException("Document not found to delete");
            }

            if (doc.getS3Key() != null) {
                s3Service.deleteFile(doc.getS3Key());
            }

            repo.delete(doc);
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete document: " + ex.getMessage());
        }
    }
}
