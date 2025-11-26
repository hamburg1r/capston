package com.document.service;



import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    private final String bucketName;
    private final String region;

    private final AmazonS3 s3Client;

    public S3Service(
            @Value("${aws.s3.bucketName}") String bucketName,
            @Value("${aws.accessKey}") String accessKey,
            @Value("${aws.secretKey}") String secretKey,
            @Value("${aws.region}") String region
    ) {
        this.bucketName = bucketName;
        this.region = region;

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public String generatePresignedUrl(String s3Key, String fileType) {

     
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 10 * 60 * 1000; // 10 minutes
        expiration.setTime(expTimeMillis);

     
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, s3Key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        generatePresignedUrlRequest.addRequestParameter("Content-Type", fileType);

  
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }
}
