package com.document.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SQSService {

    private static final Logger log = LoggerFactory.getLogger(SQSService.class);

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

    // Send document processing task to SQS
    public String sendDocumentProcessingTask(
            String documentId,
            String userId,
            String fileName,
            String fileType,
            String s3Key
    ) {
        log.info("Sending SQS processing task for documentId={} by user={}", documentId, userId);
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
                    .withDelaySeconds(0);

            SendMessageResult result = sqsClient.sendMessage(sendMessageRequest);

            log.info("SQS Processing Message Sent | MessageId={}", result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            log.error("Failed to send SQS processing task: {}", e.getMessage());
            throw new RuntimeException("Processing task failed", e);
        }
    }

    // Send metadata extraction task to SQS
    public String sendMetadataExtractionTask(
            String documentId,
            String userId,
            String s3Key,
            String fileType
    ) {
        log.info("Sending SQS metadata extraction task for documentId={} by user={}", documentId, userId);
        try {
            Map<String, Object> taskMessage = new HashMap<>();
            taskMessage.put("taskType", "EXTRACT_METADATA");
            taskMessage.put("documentId", documentId);
            taskMessage.put("userId", userId);
            taskMessage.put("s3Key", s3Key);
            taskMessage.put("fileType", fileType);
            taskMessage.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(taskMessage);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageJson)
                    .withDelaySeconds(5);

            SendMessageResult result = sqsClient.sendMessage(sendMessageRequest);

            log.info("SQS Metadata Task Sent | MessageId={}", result.getMessageId());
            return result.getMessageId();

        } catch (Exception e) {
            log.error("Failed to send SQS metadata task: {}", e.getMessage());
            throw new RuntimeException("Metadata task failed", e);
        }
    }
}
