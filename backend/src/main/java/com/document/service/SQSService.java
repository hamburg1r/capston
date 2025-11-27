package com.document.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class SQSService {

    private final AmazonSQS sqsClient;
    private final String queueUrl;
    private final ObjectMapper objectMapper;

    public SQSService(
            AmazonSQS sqsClient,
            @Value("${aws.sqs.queueUrl}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send document processing task to SQS queue
     */
    public String sendDocumentProcessingTask(
            String documentId,
            String userId,
            String fileName,
            String fileType,
            String s3Key
    ) {
        try {
            Map<String, Object> taskMessage = new HashMap<>();
            taskMessage.put("taskType", "PROCESS_DOCUMENT");
            taskMessage.put("documentId", documentId);
            taskMessage.put("userId", userId);
            taskMessage.put("fileName", fileName);
            taskMessage.put("fileType", fileType);
            taskMessage.put("s3Key", s3Key);
            taskMessage.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(taskMessage);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageJson)
                    .withDelaySeconds(0); // Process immediately

            SendMessageResult result = sqsClient.sendMessage(sendMessageRequest);

            System.out.println("SQS Message sent. MessageId: " + result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            System.err.println("Failed to send SQS message: " + e.getMessage());
            throw new RuntimeException("SQS send failed", e);
        }
    }

    /**
     * Send document for thumbnail generation
     */
    public String sendThumbnailGenerationTask(
            String documentId,
            String s3Key
    ) {
        try {
            Map<String, Object> taskMessage = new HashMap<>();
            taskMessage.put("taskType", "GENERATE_THUMBNAIL");
            taskMessage.put("documentId", documentId);
            taskMessage.put("s3Key", s3Key);
            taskMessage.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(taskMessage);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageJson);

            SendMessageResult result = sqsClient.sendMessage(sendMessageRequest);

            System.out.println("Thumbnail generation task sent. MessageId: " + result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            System.err.println("Failed to send thumbnail task: " + e.getMessage());
            throw new RuntimeException("SQS send failed", e);
        }
    }
}