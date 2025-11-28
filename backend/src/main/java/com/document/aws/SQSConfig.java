package com.document.aws;

import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class SQSConfig {

        @Value("${aws.accessKey}")
        private String awsAccessKey;

        @Value("${aws.secretKey}")
        private String awsSecretKey;

        @Value("${aws.region}")
        private String awsRegion;

        @Bean
        public AmazonSQS amazonSQS() {
                return AmazonSQSClientBuilder.standard()
                                .withRegion(awsRegion)
                                .withCredentials(
                                                new AWSStaticCredentialsProvider(
                                                                new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
                                .build();
        }

        // Add this when creating your SQS client
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                        return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
        } };
}