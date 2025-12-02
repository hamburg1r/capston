package com.document.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SNSService {

    private static final Logger log = LoggerFactory.getLogger(SNSService.class);

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

            log.info("Publishing SNS upload notification for docId={}, userId={}", documentId, userId);

            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(messageJson)
                    .withSubject("Document Upload Notification");

            PublishResult result = snsClient.publish(publishRequest);

            log.info("SNS upload notification sent. MessageId={}", result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            log.error("Failed to publish SNS upload notification: {}", e.getMessage(), e);
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

            log.info("Publishing SNS processing notification for docId={}, status={}", documentId, status);

            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(messageJson)
                    .withSubject("Document Processing Complete");

            PublishResult result = snsClient.publish(publishRequest);

            log.info("SNS processing notification sent. MessageId={}", result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            log.error("Failed to publish SNS processing notification: {}", e.getMessage(), e);
            throw new RuntimeException("SNS publish failed", e);
        }
    }
}
