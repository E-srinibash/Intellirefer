//package com.yourcompany.intellirefer.service;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.*;
//import com.yourcompany.intellirefer.exception.FileStorageException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//@Service
//public class S3Service {
//
//    @Autowired
//    private AmazonS3 s3Client;
//
//    @Value("${cloud.aws.s3.bucket.name}")
//    private String bucketName;
//
//    public String uploadFile(MultipartFile file) {
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(file.getSize());
//        metadata.setContentType(file.getContentType());
//
//        try (InputStream inputStream = file.getInputStream()) {
//            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
//            s3Client.putObject(request);
//            return s3Client.getUrl(bucketName, fileName).toString();
//        } catch (IOException e) {
//            throw new FileStorageException("Error uploading file to S3", e);
//        }
//    }
//
//    public S3ObjectInputStream downloadFile(String key) {
//        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
//        return s3Object.getObjectContent();
//    }
//
//    public void deleteFile(String key) {
//        s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
//    }
//}