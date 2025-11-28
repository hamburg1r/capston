package com.document.service;

import com.document.model.DocumentModel;
import com.document.repository.DocumentRepository;


import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
// @Transactional
public class DocumentServiceImp implements DocumentService {

	private final DocumentRepository repo;
    private final S3Service s3Service;

    public DocumentServiceImp(DocumentRepository repo, S3Service s3Service) {
        this.repo = repo;
        this.s3Service = s3Service;
    }
    // @Transactional(rollbackFor = Exception.class)
    public DocumentModel createDocument(String userId, String fileName, String fileType,String fileSize) {

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
    }

    public void updateDocument(DocumentModel doc) {
        repo.save(doc);
    }

    public DocumentModel getById(String docId, String userId) {
        return repo.findByDocumentIdAndUserId(docId, userId);
    }

    public List<DocumentModel> getUserDocuments(String userId) {
        return repo.findByUserId(userId);
    }
    public void markUploadCompleted(String documentId, String userId,
            String fileName, String fileType, String fileSize) {


		DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
		
		if (doc == null) {
		throw new RuntimeException("Document not found");
		}
		
	
		String s3Key = userId + "/" + documentId + "/" + fileName;
		
		doc.setFileName(fileName);
		doc.setFileType(fileType);
		doc.setFileSize(fileSize);
		doc.setS3Key(s3Key);
		doc.setStatus("COMPLETED");
		
		repo.save(doc);
		}
    public String generateDownloadUrl(String documentId, String userId) {
        DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
        if (doc == null) {
            throw new RuntimeException("Document Not Found");
        }

        return s3Service.generateDownloadUrl(doc.getS3Key());
    }
    public void deleteDocument(String documentId, String userId) {

        DocumentModel doc = repo.findByDocumentIdAndUserId(documentId, userId);
        if (doc == null) {
            throw new RuntimeException("Document not found or not owned by user");
        }

        
        if (doc.getS3Key() != null) {
            s3Service.deleteFile(doc.getS3Key());
        }

       
        repo.delete(doc);
    }
   

}
