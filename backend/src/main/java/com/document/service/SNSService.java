package com.document.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class SNSService {

    private final AmazonSNS snsClient;
    private final String topicArn;
    private final ObjectMapper objectMapper;

    public SNSService(
            AmazonSNS snsClient,
            @Value("${aws.sns.topicArn}") String topicArn
    ) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Publish document upload notification to SNS
     */
    public String publishDocumentUploadNotification(
            String documentId,
            String userId,
            String fileName,
            String fileType,
            String fileSize
    ) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", "DOCUMENT_UPLOADED");
            message.put("documentId", documentId);
            message.put("userId", userId);
            message.put("fileName", fileName);
            message.put("fileType", fileType);
            message.put("fileSize", fileSize);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);

            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(messageJson)
                    .withSubject("Document Upload Notification");

            PublishResult result = snsClient.publish(publishRequest);

            System.out.println("SNS Message published. MessageId: " + result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            System.err.println("Failed to publish SNS message: " + e.getMessage());
            throw new RuntimeException("SNS publish failed", e);
        }
    }

    /**
     * Publish document processing completion notification
     */
    public String publishDocumentProcessingComplete(
            String documentId,
            String userId,
            String status
    ) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", "DOCUMENT_PROCESSING_COMPLETE");
            message.put("documentId", documentId);
            message.put("userId", userId);
            message.put("status", status);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);

            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(messageJson)
                    .withSubject("Document Processing Complete");

            PublishResult result = snsClient.publish(publishRequest);

            System.out.println("Processing complete notification sent. MessageId: " + result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            System.err.println("Failed to publish processing notification: " + e.getMessage());
            throw new RuntimeException("SNS publish failed", e);
        }
    }
}