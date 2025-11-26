package com.document.service;

import java.util.List;

import com.document.model.DocumentModel;

public interface DocumentService {
	public DocumentModel createDocument(String userId, String fileName, String fileType,String fileSize);
	public void updateDocument(DocumentModel doc);
	public DocumentModel getById(String docId, String userId);
	public List<DocumentModel> getUserDocuments(String userId);
	public void markUploadCompleted(String documentId, String userId,
            String fileName, String fileType, String fileSize) ;
}
