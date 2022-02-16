package com.example.amazons3.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.amazons3.service.S3StorageService;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation("An API to get the list of buckets")
    @GetMapping(path = "/buckets")
    public List<Bucket> listBuckets(){
        return s3StorageService.getAllBuckets();
    }

    @ApiOperation("An API to upload file by saving file on local first")
    @PostMapping(path = "/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String,String> uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
        s3StorageService.uploadFile(file.getOriginalFilename(),file.getBytes());
        Map<String,String> result = new HashMap<>();
        result.put("key",file.getOriginalFilename());
        return result;
    }

    @ApiOperation("An API to upload file without saving file on local")
    @PostMapping(path = "/upload/without-save",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String,String> uploadFileWithoutSavingFileOnLocal(@RequestPart(value = "file") MultipartFile file) {
        s3StorageService.uploadFileWithoutSavingFileOnLocal(file);
        Map<String,String> result = new HashMap<>();
        result.put("key",file.getOriginalFilename());
        return result;
    }

    @ApiOperation("An API to fetch the metadata of an object without actually fetching the object")
    @GetMapping(path = "/buckets/head-object")
    public ObjectMetadata getObjectMetadata(@RequestParam String key) {
        return s3StorageService.getObjectMetadata(key);
    }

    @ApiOperation("An API to download a file from S3")
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

    @ApiOperation("An API to get a presigned URL for an object with one minute validity")
    @GetMapping(path = "/download/presigned-url")
    public String getPresignedUrlForObject(@RequestParam String key,
                                    @RequestParam String bucketName) {
        return s3StorageService.getPresignedUrlForObject(bucketName, key);
    }

    @ApiOperation("An API to check if the bucket/object exists")
    @GetMapping(path = "/buckets/exists")
    public boolean checkIfObjectExists(@RequestParam String key,
                                      @RequestParam String bucketName) {
        return s3StorageService.checkIfObjectExists(bucketName, key);
    }
}