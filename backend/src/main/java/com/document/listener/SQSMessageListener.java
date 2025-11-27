package com.document.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.document.service.DocumentServiceImp;
import com.document.service.SNSService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SQSMessageListener {

    private final AmazonSQS sqsClient;
    private final String queueUrl;
    private final DocumentServiceImp documentService;
    private final SNSService snsService;
    private final ObjectMapper objectMapper;

    public SQSMessageListener(
            AmazonSQS sqsClient,
            @Value("${aws.sqs.queueUrl}") String queueUrl,
            DocumentServiceImp documentService,
            SNSService snsService
    ) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.documentService = documentService;
        this.snsService = snsService;
        this.objectMapper = new ObjectMapper();
    }

    
    //  Poll SQS queue every 10 seconds for new messages
    
    @Scheduled(fixedDelay = 10000)
    public void pollMessages() {
        try {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(10)
                    .withWaitTimeSeconds(5); // Long polling

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();

            for (Message message : messages) {
                processMessage(message);
                deleteMessage(message);
            }

        } catch (Exception e) {
            System.err.println("Error polling SQS: " + e.getMessage());
        }
    }

    
    //  Process individual SQS message
    
    private void processMessage(Message message) {
        try {
            String body = message.getBody();
            Map<String, Object> taskData = objectMapper.readValue(body, Map.class);

            String taskType = (String) taskData.get("taskType");

            switch (taskType) {
                case "PROCESS_DOCUMENT":
                    processDocument(taskData);
                    break;
                 case "EXTRACT_METADATA":
                    extractMetadata(taskData);
                    break;
                default:
                    System.out.println("Unknown task type: " + taskType);
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    
    //  Process document (simulate processing)
    
    private void processDocument(Map<String, Object> taskData) {
        String documentId = (String) taskData.get("documentId");
        String userId = (String) taskData.get("userId");

        System.out.println("Processing document: " + documentId);

        try {
            Thread.sleep(2000); // Simulate processing time

            // Update document status
            var doc = documentService.getById(documentId, userId);
            if (doc != null) {
                doc.setStatus("PROCESSING");
                documentService.updateDocument(doc);

                // Simulate completion
                Thread.sleep(3000);
                doc.setStatus("COMPLETED");
                documentService.updateDocument(doc);

                // Send completion notification via SNS
                snsService.publishDocumentProcessingComplete(documentId, userId, "COMPLETED");
            }

        } catch (Exception e) {
            System.err.println("Document processing failed: " + e.getMessage());
        }
    }

    
    
    //  Extract metadata from document
     
    private void extractMetadata(Map<String, Object> taskData) {
        String documentId = (String) taskData.get("documentId");
        String userId = (String) taskData.get("userId");
        String s3Key = (String) taskData.get("s3Key");
        String fileType = (String) taskData.get("fileType");

        System.out.println("Extracting metadata from document: " + documentId);

        try {
            Thread.sleep(2000);

            var doc = documentService.getById(documentId, userId);
            if (doc != null) {
                // Simulate extracted metadata
                System.out.println("Extracted metadata:");
                System.out.println("  - Author: John Doe");
                System.out.println("  - Created Date: 2024-01-15");
                System.out.println("  - Page Count: 12");
                System.out.println("  - File Type: " + fileType);

                //  need to implement this in dynamo db  to get real metadata
                doc.setStatus("METADATA_EXTRACTED");
                documentService.updateDocument(doc);

                System.out.println("Metadata extraction completed for: " + s3Key);
            }

        } catch (Exception e) {
            System.err.println("Metadata extraction failed: " + e.getMessage());
        }
    }

    
    //  Delete processed message from queue
    
    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteRequest = new DeleteMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(message.getReceiptHandle());

            sqsClient.deleteMessage(deleteRequest);
            System.out.println("Message deleted from queue");

        } catch (Exception e) {
            System.err.println("Failed to delete message: " + e.getMessage());
        }
    }
}