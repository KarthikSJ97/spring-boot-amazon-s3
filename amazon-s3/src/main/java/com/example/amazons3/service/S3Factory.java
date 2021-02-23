package com.example.amazons3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
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

    public void uploadFile(String name,byte[] content)  {

//      TODO: Try to avoid storing it in file before uploading it to S3
        File file = new File("src/main/resources/"+name);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content);
            amazonS3Client.putObject(defaultBucketName, defaultBaseFolder+"/"+file.getName(), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
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
