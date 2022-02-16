package com.example.amazons3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class S3Factory {

    @Autowired
    AmazonS3 amazonS3Client;

    @Value("${s3.bucket.name}")
    String defaultBucketName;

    @Value("${s3.default.folder}")
    String defaultBaseFolder;

    public List<Bucket> getAllBuckets() {
        return amazonS3Client.listBuckets();
    }

    public void uploadFile(String name,byte[] content) throws IOException {

        File file = new File("src/main/resources/"+name);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try(fileOutputStream) {
            fileOutputStream.write(content);
            amazonS3Client.putObject(defaultBucketName, defaultBaseFolder+"/"+file.getName(), file);
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
        return null;
    }

}
