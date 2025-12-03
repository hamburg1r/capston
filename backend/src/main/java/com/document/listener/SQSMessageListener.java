package com.document.listener;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.document.model.DocumentModel;
import com.document.service.DocumentServiceImp;
import com.document.service.SNSService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class SQSMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SQSMessageListener.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final AmazonSQS sqsClient;
    private final AmazonS3 s3Client;
    private final String queueUrl;
    private final String bucketName;
    private final DocumentServiceImp documentService;
    private final SNSService snsService;
    private final ObjectMapper objectMapper;

    public SQSMessageListener(
            AmazonSQS sqsClient,
            AmazonS3 s3Client,
            @Value("${aws.sqs.queueUrl}") String queueUrl,
            @Value("${aws.s3.bucketName}") String bucketName,
            DocumentServiceImp documentService,
            SNSService snsService) {
        this.sqsClient = sqsClient;
        this.s3Client = s3Client;
        this.queueUrl = queueUrl;
        this.bucketName = bucketName;
        this.documentService = documentService;
        this.snsService = snsService;
        this.objectMapper = new ObjectMapper();
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void pollMessages() {
        try {
            log.debug("Polling SQS queue: {}", queueUrl);

            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(10)
                    .withWaitTimeSeconds(10) // Long polling
                    .withVisibilityTimeout(300) // 5 minutes to process
                    .withAttributeNames("All")
                    .withMessageAttributeNames("All");

            ReceiveMessageResult result = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = result.getMessages();

            if (!messages.isEmpty()) {
                log.info("Received {} message(s) from SQS queue", messages.size());

                for (Message message : messages) {
                    processMessageAsync(message);
                }
            }

        } catch (Exception e) {
            log.error("Error polling SQS queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Process message asynchronously
     */
    @Async
    public CompletableFuture<Void> processMessageAsync(Message message) {
        return CompletableFuture.runAsync(() -> {
            try {
                processMessage(message);
                deleteMessage(message);
            } catch (Exception e) {
                log.error("Failed to process message {}: {}", message.getMessageId(), e.getMessage(), e);
                handleFailedMessage(message);
            }
        });
    }

    /**
     * Process individual SQS message
     */
    private void processMessage(Message message) {
        String messageId = message.getMessageId();

        try {
            String body = message.getBody();
            log.info("Processing message: {} with body length: {}", messageId, body.length());

            // Parse SNS message wrapper if present
            Map<String, Object> messageData = objectMapper.readValue(body, Map.class);

            // Check if this is an SNS notification
            if (messageData.containsKey("Type") && "Notification".equals(messageData.get("Type"))) {
                String snsMessage = (String) messageData.get("Message");
                messageData = objectMapper.readValue(snsMessage, Map.class);
            }

            Map<String, Object> taskData = messageData;
            String taskType = (String) taskData.get("taskType");

            if (taskType == null) {
                log.warn("Message has no taskType, checking for eventType");
                taskType = (String) taskData.get("eventType");
            }

            if (taskType == null) {
                log.error("Message {} has no valid taskType or eventType. Skipping.", messageId);
                return;
            }

            log.info("Processing task type: {} for message: {}", taskType, messageId);

            switch (taskType) {
                case "PROCESS_DOCUMENT":
                case "DOCUMENT_UPLOADED":
                    processDocument(taskData);
                    break;
                case "EXTRACT_METADATA":
                    extractMetadata(taskData);
                    break;
                default:
                    log.warn("Unknown task type: {} for message: {}", taskType, messageId);
            }

        } catch (Exception e) {
            log.error("Error processing message {}: {}", messageId, e.getMessage(), e);
            throw new RuntimeException("Message processing failed", e);
        }
    }

    /**
     * Process document with actual implementation
     */
    private void processDocument(Map<String, Object> taskData) {
        String documentId = (String) taskData.get("documentId");
        String userId = (String) taskData.get("userId");
        String fileName = (String) taskData.get("fileName");

        log.info("Starting document processing: docId={}, userId={}, file={}", documentId, userId, fileName);

        DocumentModel doc = null;

        try {
            // Get document from database
            doc = documentService.getById(documentId, userId);
            if (doc == null) {
                log.error("Document not found: documentId={}, userId={}", documentId, userId);
                return;
            }

            // Update status to PROCESSING
            doc.setStatus("PROCESSING");
            documentService.updateDocument(doc);
            log.info("Document status updated to PROCESSING: {}", documentId);

            // Verify file exists in S3
            String s3Key = doc.getS3Key();
            if (s3Key == null) {
                s3Key = userId + "/" + documentId + "/" + fileName;
                doc.setS3Key(s3Key);
            }

            boolean fileExists = verifyS3FileExists(s3Key);
            if (!fileExists) {
                log.error("File not found in S3: {}", s3Key);
                doc.setStatus("FAILED");
                documentService.updateDocument(doc);
                snsService.publishDocumentProcessingComplete(documentId, userId, "FAILED");
                return;
            }

            // Perform actual processing based on file type
            String fileType = doc.getFileType();
            Map<String, String> processingResult = performFileProcessing(s3Key, fileType);

            // Extract and save metadata
            Map<String, String> metadata = extractFileMetadata(s3Key, fileType);

            // Update document with results
            // added
            doc.setProcessingInfo(processingResult);
            doc.setMetadata(metadata);
            // ..............
            doc.setStatus("COMPLETED");
            documentService.updateDocument(doc);

            log.info("Document processing completed successfully: {}", documentId);
            log.info("Processing results: {}", processingResult);
            log.info("Extracted metadata: {}", metadata);



            // Send success notification
            snsService.publishDocumentProcessingComplete(documentId, userId, "COMPLETED");

        } catch (Exception e) {
            log.error("Document processing failed for {}: {}", documentId, e.getMessage(), e);

            if (doc != null) {
                try {
                    doc.setStatus("FAILED");
                    documentService.updateDocument(doc);
                    snsService.publishDocumentProcessingComplete(documentId, userId, "FAILED");
                } catch (Exception ex) {
                    log.error("Failed to update failure status: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Verify if file exists in S3
     */
    private boolean verifyS3FileExists(String s3Key) {
        try {
            s3Client.getObjectMetadata(bucketName, s3Key);
            log.debug("File verified in S3: {}", s3Key);
            return true;
        } catch (Exception e) {
            log.error("File not found in S3: {}", s3Key);
            return false;
        }
    }

    /**
     * Perform actual file processing based on type
     */
    private Map<String, String> performFileProcessing(String s3Key, String fileType) {
        Map<String, String> result = new HashMap<>();

        try {
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, s3Key));

            switch (fileType.toLowerCase()) {
                case "application/pdf":
                    result = processPDF(s3Object.getObjectContent());
                    break;
                case "image/jpeg":
                case "image/jpg":
                case "image/png":
                    result = processImage(s3Object.getObjectContent(), fileType);
                    break;
                case "text/plain":
                    result = processTextFile(s3Object.getObjectContent());
                    break;
                default:
                    result.put("status", "BASIC_PROCESSING");
                    result.put("message", "Basic processing completed for " + fileType);
            }

            s3Object.close();

        } catch (Exception e) {
            log.error("File processing failed for {}: {}", s3Key, e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Process PDF files
     */
    private Map<String, String> processPDF(InputStream inputStream) {
        Map<String, String> result = new HashMap<>();

        try (PDDocument document = PDDocument.load(inputStream)) {
            int pageCount = document.getNumberOfPages();
            PDDocumentInformation info = document.getDocumentInformation();

            result.put("pageCount", String.valueOf(pageCount));
            result.put("title", info.getTitle() != null ? info.getTitle() : "N/A");
            result.put("author", info.getAuthor() != null ? info.getAuthor() : "N/A");
            result.put("subject", info.getSubject() != null ? info.getSubject() : "N/A");
            result.put("keywords", info.getKeywords() != null ? info.getKeywords() : "N/A");
            result.put("creator", info.getCreator() != null ? info.getCreator() : "N/A");
            result.put("producer", info.getProducer() != null ? info.getProducer() : "N/A");
            result.put("creationDate", info.getCreationDate() != null ? info.getCreationDate().toString() : "N/A");
            result.put("status", "SUCCESS");

            log.info("PDF processing completed: {} pages", pageCount);

        } catch (Exception e) {
            log.error("PDF processing failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Process image files
     */
    private Map<String, String> processImage(InputStream inputStream, String fileType) {
        Map<String, String> result = new HashMap<>();

        try {
            BufferedImage image = ImageIO.read(inputStream);

            if (image != null) {
                result.put("width", String.valueOf(image.getWidth()));
                result.put("height", String.valueOf(image.getHeight()));
                result.put("colorModel", image.getColorModel().toString());
                result.put("pixelSize", String.valueOf(image.getWidth() * image.getHeight()));
                result.put("hasAlpha", String.valueOf(image.getColorModel().hasAlpha()));
                result.put("status", "SUCCESS");

                log.info("Image processing completed: {}x{}", image.getWidth(), image.getHeight());
            } else {
                result.put("status", "ERROR");
                result.put("error", "Failed to read image");
            }

        } catch (Exception e) {
            log.error("Image processing failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Process text files
     */
    private Map<String, String> processTextFile(InputStream inputStream) {
        Map<String, String> result = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int lineCount = 0;
            int wordCount = 0;
            int charCount = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length();
                wordCount += line.split("\\s+").length;
            }

            result.put("lineCount", String.valueOf(lineCount));
            result.put("wordCount", String.valueOf(wordCount));
            result.put("charCount", String.valueOf(charCount));
            result.put("status", "SUCCESS");

            log.info("Text file processing completed: {} lines, {} words", lineCount, wordCount);

        } catch (Exception e) {
            log.error("Text file processing failed: {}", e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Extract metadata from document
     */
    private void extractMetadata(Map<String, Object> taskData) {
        String documentId = (String) taskData.get("documentId");
        String userId = (String) taskData.get("userId");
        String s3Key = (String) taskData.get("s3Key");
        String fileType = (String) taskData.get("fileType");

        log.info("Starting metadata extraction: docId={}, s3Key={}", documentId, s3Key);

        try {
            DocumentModel doc = documentService.getById(documentId, userId);
            if (doc == null) {
                log.error("Document not found for metadata extraction: {}", documentId);
                return;
            }

            Map<String, String> metadata = extractFileMetadata(s3Key, fileType);

            doc.setStatus("METADATA_EXTRACTED");
            documentService.updateDocument(doc);

            log.info("Metadata extraction completed for {}: {}", documentId, metadata);

            snsService.publishDocumentProcessingComplete(documentId, userId, "METADATA_EXTRACTED");

        } catch (Exception e) {
            log.error("Metadata extraction failed for {}: {}", documentId, e.getMessage(), e);
        }
    }

    /**
     * Extract file metadata from S3
     */
    private Map<String, String> extractFileMetadata(String s3Key, String fileType) {
        Map<String, String> metadata = new HashMap<>();

        try {
            ObjectMetadata s3Metadata = s3Client.getObjectMetadata(bucketName, s3Key);

            metadata.put("contentLength", String.valueOf(s3Metadata.getContentLength()));
            metadata.put("contentType", s3Metadata.getContentType());
            metadata.put("eTag", s3Metadata.getETag());
            metadata.put("lastModified", s3Metadata.getLastModified().toString());

            if (s3Metadata.getUserMetadata() != null) {
                metadata.putAll(s3Metadata.getUserMetadata());
            }

            log.debug("S3 metadata extracted for {}: {}", s3Key, metadata);

        } catch (Exception e) {
            log.error("Failed to extract S3 metadata for {}: {}", s3Key, e.getMessage(), e);
            metadata.put("error", e.getMessage());
        }

        return metadata;
    }

    /**
     * Delete successfully processed message from queue
     */
    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteRequest = new DeleteMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(message.getReceiptHandle());

            sqsClient.deleteMessage(deleteRequest);
            log.debug("Message deleted from queue: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("Failed to delete message {}: {}", message.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Handle failed messages - check retry count and move to DLQ if needed
     */
    private void handleFailedMessage(Message message) {
        try {
            String receiveCount = message.getAttributes().get("ApproximateReceiveCount");
            int retryCount = receiveCount != null ? Integer.parseInt(receiveCount) : 0;

            if (retryCount >= MAX_RETRY_ATTEMPTS) {
                log.error("Message {} exceeded retry limit ({} attempts). Moving to DLQ.",
                        message.getMessageId(), retryCount);
                deleteMessage(message);
            } else {
                log.warn("Message {} failed, will be retried. Attempt: {}/{}",
                        message.getMessageId(), retryCount, MAX_RETRY_ATTEMPTS);
                // Message will automatically be retried by SQS after visibility timeout
            }

        } catch (Exception e) {
            log.error("Error handling failed message: {}", e.getMessage(), e);
        }
    }

    /**
     * Change message visibility (useful for extending processing time)
     */
    private void extendMessageVisibility(Message message, int seconds) {
        try {
            ChangeMessageVisibilityRequest request = new ChangeMessageVisibilityRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(message.getReceiptHandle())
                    .withVisibilityTimeout(seconds);

            sqsClient.changeMessageVisibility(request);
            log.debug("Extended visibility timeout for message {} to {} seconds",
                    message.getMessageId(), seconds);

        } catch (Exception e) {
            log.error("Failed to extend visibility: {}", e.getMessage(), e);
        }
    }
}