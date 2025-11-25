package com.example.capstone.repo;

import com.example.capstone.model.DocumentItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class DocumentRepository {

    private final DynamoDbTable<DocumentItem> table;

    public DocumentRepository(DynamoDbClient dynamo,
                              @Value("${aws.dynamodb.documentsTable}") String tableName) {

        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamo)
                .build();

        this.table = enhanced.table(tableName, TableSchema.fromBean(DocumentItem.class));
    }

    /**
     * Save new DocumentItem into DynamoDB
     */
    public void save(DocumentItem doc) {
        table.putItem(doc);
    }

    /**
     * Update only the status attribute atomically
     */
    public void updateStatus(String documentId, String userId, String status) {

        Key key = Key.builder()
                .partitionValue(documentId)
                .sortValue(userId)
                .build();

        UpdateItemEnhancedRequest<DocumentItem> request =
                UpdateItemEnhancedRequest.builder(DocumentItem.class)
                        .item(new DocumentItem()) // can also use builder
                        .ignoreNulls(true)
                        .updateExpression(Expression.builder()
                                .expression("SET #st = :s")
                                .putExpressionName("#st", "status")
                                .putExpressionValue(":s", AttributeValue.fromS(status))
                                .build())
                        .key(key)
                        .build();

        table.updateItem(request);
    }

    /**
     * Retrieve a document based on (documentId, userId)
     */
    public DocumentItem find(String documentId, String userId) {
        Key key = Key.builder()
                .partitionValue(documentId)
                .sortValue(userId)
                .build();

        return table.getItem(key);
    }
}

