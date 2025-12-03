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

    @Override
    public DocumentModel createDocument(String userId, String fileName, String fileType, String fileSize) {
        log.info("Creating document for userId={}, fileName={}", userId, fileName);

        DocumentModel doc = new DocumentModel();
        doc.setDocumentId(UUID.randomUUID().toString());
        doc.setUserId(userId);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(fileSize);
        doc.setStatus("UPLOADED");
        doc.setUploadDate(LocalDateTime.now().toString());

        repo.save(doc);
        log.info("Document created successfully: docId={}", doc.getDocumentId());
        return doc;
    }

    @Override
    public void updateDocument(DocumentModel doc) {
        log.info("Updating document docId={}", doc.getDocumentId());
        repo.save(doc);
    }

    @Override
    public DocumentModel getById(String docId, String userId) {
        log.info("Searching document docId={} userId={}", docId, userId);

        DocumentModel doc = repo.findByDocumentIdAndUserId(docId, userId);

        if (doc == null) {
            log.warn("Document not found: {}", docId);
            throw new DocumentNotFoundException(docId, userId); // 🔹CUSTOM EXCEPTION
        }
        return doc;
    }

    @Override
    public List<DocumentModel> getUserDocuments(String userId) {
        log.info("Fetching documents for user={}", userId);
        return repo.findByUserId(userId);
    }

    @Override
    public void markUploadCompleted(String documentId, String userId,
                                   String fileName, String fileType, String fileSize) {

        log.info("Marking upload complete for docId={} userId={}", documentId, userId);

        DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);

        if (doc == null) {
            throw new DocumentNotFoundException(documentId, userId);
        }

        String s3Key = userId + "/" + documentId + "/" + fileName;

        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(fileSize);
        doc.setS3Key(s3Key);
        // doc.setStatus("COMPLETED");
        doc.setStatus("UPDATED");


        repo.save(doc);
    }

    @Override
    public String generateDownloadUrl(String documentId, String userId) {
        log.info("Generating download URL for docId={}", documentId);

        DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
        if (doc == null) {
            throw new DocumentNotFoundException(documentId, userId);
        }

        return s3Service.generateDownloadUrl(doc.getS3Key());
    }

    @Override
    public void deleteDocument(String documentId, String userId) {
        log.info("Deleting document docId={}", documentId);

        DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);

        if (doc == null) {
            throw new DocumentNotFoundException(documentId, userId);
        }

        if (doc.getS3Key() != null) {
            s3Service.deleteFile(doc.getS3Key());
            log.info("S3 file deleted: {}", doc.getS3Key());
        }

        repo.delete(doc);
        log.info("Document deleted from DB docId={}", documentId);
    }
}
