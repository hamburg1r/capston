package com.document.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.document.exception.DocumentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final String bucketName;
    private final AmazonS3 s3Client;

    public S3Service(
            @Value("${aws.s3.bucketName}") String bucketName,
            @Value("${aws.accessKey}") String accessKey,
            @Value("${aws.secretKey}") String secretKey,
            @Value("${aws.region}") String region
    ) {
        this.bucketName = bucketName;

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public String generatePresignedUrl(String s3Key, String fileType) {
        log.info("Generating upload URL for s3Key: {}", s3Key);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000);

            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucketName, s3Key)
                            .withMethod(HttpMethod.PUT)
                            .withExpiration(expiration);

            req.addRequestParameter("Content-Type", fileType);

            URL url = s3Client.generatePresignedUrl(req);

            log.info("Upload URL generated successfully for {}", s3Key);
            return url.toString();

        } catch (Exception ex) {
            log.error("Error generating presigned upload URL for {}: {}", s3Key, ex.getMessage());
            throw new RuntimeException("Failed to generate upload URL: " + ex.getMessage());
        }
    }

    public String generateDownloadUrl(String s3Key) {
        log.info("Generating download URL for s3Key: {}", s3Key);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucketName, s3Key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(req);

            log.info("Download URL generated successfully for {}", s3Key);
            return url.toString();

        } catch (Exception ex) {
            log.error("Error generating download URL for {}: {}", s3Key, ex.getMessage());
            throw new RuntimeException("Failed to generate download URL: " + ex.getMessage());
        }
    }

    public void deleteFile(String s3Key) {
        log.warn("Deleting file from S3: {}", s3Key);
        try {
            s3Client.deleteObject(bucketName, s3Key);
            log.info("File deleted successfully: {}", s3Key);

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                log.warn("File not found in S3 while deleting: {}", s3Key);
                throw new DocumentNotFoundException("File not found in S3");
            }
            log.error("AmazonS3Exception: Failed to delete {}: {}", s3Key, e.getMessage());
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error deleting {}: {}", s3Key, ex.getMessage());
            throw new RuntimeException("Failed to delete file: " + ex.getMessage());
        }
    }
}
