package com.example.amazons3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class S3StorageService {

    @Autowired
    AmazonS3 amazonS3Client;

    @Value("${s3.bucket.name}")
    String defaultBucketName;

    @Value("${s3.default.folder}")
    String defaultBaseFolder;

    public List<Bucket> getAllBuckets() {
        return amazonS3Client.listBuckets();
    }

    public ObjectListing getObjectsInBucket(String bucketName) {
        try {
            return amazonS3Client.listObjects(bucketName);
        } catch (Exception e) {
            log.error("Specified bucket does not exists");
            return new ObjectListing();
        }
    }

    public void uploadFile(String name,byte[] content) throws IOException {

        File file = new File("src/main/resources/"+name);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try(fileOutputStream) {
            fileOutputStream.write(content);
            amazonS3Client.putObject(defaultBucketName, defaultBaseFolder+"/"+file.getName()+new Date().toInstant(), file);
        } catch (IOException e) {
            log.error("Some error occurred while uploading file to S3...");
        } finally {
            deleteLocallyStoredFile(file);
        }
    }

    @Async
    private void deleteLocallyStoredFile(File file) {
        CompletableFuture.runAsync(() -> {
            boolean fileDeleted = false;
            try {
                fileDeleted = Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("Error occurred while deleting file: {}", file.getPath());
            }
            log.info("File deletion status: {}", fileDeleted);
        });
    }

    public void uploadFileWithoutSavingFileOnLocal(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.addUserMetadata("fileName", file.getOriginalFilename());
        try {
            amazonS3Client.putObject(defaultBucketName, defaultBaseFolder+"/"+file.getName()+new Date().toInstant(), file.getInputStream(), objectMetadata);
        } catch (Exception e) {
            log.error("Some error occurred while uploading file to S3...");
        }
    }

    public ObjectMetadata getObjectMetadata(String key) {
        if(checkIfObjectExists(defaultBucketName, key)) {
            return amazonS3Client.getObjectMetadata(defaultBucketName, key);
        } else {
            return new ObjectMetadata();
        }
    }

    public byte[] getFile(String key) {
        S3Object obj = amazonS3Client.getObject(defaultBucketName, defaultBaseFolder+"/"+key);
        S3ObjectInputStream stream = obj.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(stream);
            obj.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public String getPresignedUrlForObject(String bucketName, String key) {
        try {
            Date expiration = new Date();
            long expTimeMillis = Instant.now().toEpochMilli();
            expTimeMillis += 1000 * 60;
            expiration.setTime(expTimeMillis);
            if(checkIfObjectExists(bucketName, key)) {
                return amazonS3Client.generatePresignedUrl(bucketName, key, expiration).toString();
            } else {
                return "Requested object does not exist";
            }
        } catch (Exception e) {
            log.error("Something went wrong while generating presigned URL for the requested object");
            return null;
        }
    }

    public boolean checkIfObjectExists(String bucketName, String key) {
        return amazonS3Client.doesObjectExist(bucketName, key);
    }
}
