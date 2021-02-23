package com.example.amazons3.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.example.amazons3.service.S3Factory;
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
    S3Factory s3Factory;

    @GetMapping(path = "/buckets")
    public List<Bucket> listBuckets(){
        return s3Factory.getAllBuckets();
    }

    @PostMapping(path = "/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String,String> uploadFile(@RequestPart(value = "file", required = false) MultipartFile files) throws IOException {
        s3Factory.uploadFile(files.getOriginalFilename(),files.getBytes());
        Map<String,String> result = new HashMap<>();
        result.put("key",files.getOriginalFilename());
        return result;
    }

    @GetMapping(path = "/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value = "file") String file) {
        byte[] data = s3Factory.getFile(file);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "text/plain")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);
    }
}