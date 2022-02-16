package com.example.amazons3.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.amazons3.service.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class S3StorageController {
    @Autowired
    S3StorageService s3StorageService;

    @GetMapping(path = "/buckets")
    public List<Bucket> listBuckets(){
        return s3StorageService.getAllBuckets();
    }

    @PostMapping(path = "/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String,String> uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
        s3StorageService.uploadFile(file.getOriginalFilename(),file.getBytes());
        Map<String,String> result = new HashMap<>();
        result.put("key",file.getOriginalFilename());
        return result;
    }

    @PostMapping(path = "/upload/without-save",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String,String> uploadFileWithoutSavingFileOnLocal(@RequestPart(value = "file") MultipartFile file) throws IOException {
        s3StorageService.uploadFileWithoutSavingFileOnLocal(file);
        Map<String,String> result = new HashMap<>();
        result.put("key",file.getOriginalFilename());
        return result;
    }

    @GetMapping(path = "/head-object")
    public ObjectMetadata getObjectMetadata(@RequestParam String key) {
        return s3StorageService.getObjectMetadata(key);
    }

    @GetMapping(path = "/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value = "file") String file) {
        byte[] data = s3StorageService.getFile(file);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "text/plain")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);
    }

    @GetMapping(path = "/download/presigned-url")
    public String getPresignedUrlForObject(@RequestParam String key,
                                    @RequestParam String bucketName) {
        return s3StorageService.getPresignedUrlForObject(bucketName, key);
    }

    @GetMapping(path = "/buckets/exists")
    public boolean checkIfObjectExists(@RequestParam String key,
                                      @RequestParam String bucketName) {
        return s3StorageService.checkIfObjectExists(bucketName, key);
    }
}