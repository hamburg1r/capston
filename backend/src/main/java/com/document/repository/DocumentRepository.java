package com.document.repository;



import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.document.model.DocumentModel;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public DocumentRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(DocumentModel documentModel) {
        dynamoDBMapper.save(documentModel);
    }

    public List<DocumentModel> findByUserId(String userId) {
        DocumentModel doc = new DocumentModel();
        doc.setUserId(userId);

        DynamoDBQueryExpression<DocumentModel> query = new DynamoDBQueryExpression<DocumentModel>()
                .withHashKeyValues(doc);

        return dynamoDBMapper.query(DocumentModel.class, query);
    }

    public DocumentModel findByDocumentIdAndUserId(String documentId, String userId) {
        return dynamoDBMapper.load(DocumentModel.class, documentId, userId);
    }
}
