package com.example.amazons3.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Value("${s3.access.name}")
    String accessKey;
    @Value("${s3.access.secret}")
    String accessSecret;
    @Value("${s3.region.name}")
    String region;

    @Bean
    public AmazonS3 generateS3Client() {
        BasicAWSCredentials cred = new BasicAWSCredentials(accessKey, accessSecret);
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(cred))
                .withRegion(region)
                .build();
    }

}