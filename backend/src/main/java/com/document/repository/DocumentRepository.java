package com.document.repository;



import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.document.model.DocumentModel;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":uid", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :uid")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.scan(DocumentModel.class, scanExpression);
    }

    public DocumentModel findByDocumentIdAndUserId(String documentId, String userId) {
        return dynamoDBMapper.load(DocumentModel.class, documentId, userId);
    }
    

    public void delete(DocumentModel doc) {
        dynamoDBMapper.delete(doc);
    }
}














