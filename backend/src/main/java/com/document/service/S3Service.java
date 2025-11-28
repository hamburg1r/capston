package com.document.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.document.exception.DocumentNotFoundException;
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
        try {
            Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000);

            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucketName, s3Key)
                            .withMethod(HttpMethod.PUT)
                            .withExpiration(expiration);

            req.addRequestParameter("Content-Type", fileType);

            URL url = s3Client.generatePresignedUrl(req);
            return url.toString();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate upload URL: " + ex.getMessage());
        }
    }

    public String generateDownloadUrl(String s3Key) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucketName, s3Key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(req);
            return url.toString();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate download URL: " + ex.getMessage());
        }
    }

    public void deleteFile(String s3Key) {
        try {
            s3Client.deleteObject(bucketName, s3Key);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new DocumentNotFoundException("File not found in S3");
            }
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete file: " + ex.getMessage());
        }
    }
}
