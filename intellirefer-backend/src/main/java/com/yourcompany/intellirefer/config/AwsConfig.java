//package com.yourcompany.intellirefer.config;
//
//
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.regions.Regions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class AwsConfig {
//
//    @Value("${cloud.aws.credentials.accessKey}")
//    private String awsAccessKey;
//
//    @Value("${cloud.aws.credentials.secretKey}")
//    private String awsSecretKey;
//
//    @Value("${cloud.aws.region.static}")
//    private String awsRegion;
//
//    @Bean
//    public AmazonS3 s3Client() {
//        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
//        return AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withRegion(Regions.fromName(awsRegion))
//                .build();
//    }
//}
