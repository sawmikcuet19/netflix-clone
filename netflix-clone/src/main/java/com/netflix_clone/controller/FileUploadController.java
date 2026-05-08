package com.netflix_clone.controller;

import com.netflix_clone.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, String>> uploadVideo(
            @RequestParam("file") MultipartFile file
    ) {
        String uuid = fileUploadService.storeVideoFile(file);

        return ResponseEntity.ok(buildUploadresponse(uuid, file));
    }

    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String uuid = fileUploadService.storeImageFile(file);

        return ResponseEntity.ok(buildUploadresponse(uuid, file));
    }

    @GetMapping("/video/{uuid}")
    public ResponseEntity<Resource> shareVideo(
            @PathVariable String uuid,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @RequestHeader(value = "token", required = false) String tokenParam
    ) {
        return fileUploadService.serveVideo(uuid, rangeHeader);
    }

    @GetMapping("/image/{uuid}")
    public ResponseEntity<Resource> shareImage(
            @PathVariable String uuid
            //@RequestHeader(value = "Range", required = false) String rangeHeader,
           // @RequestHeader(value = "token", required = false) String tokenParam
    ) {
        return fileUploadService.serveImage(uuid);
    }

    private Map<String, String> buildUploadresponse(String uuid, MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        response.put("uuid", uuid);
        response.put("fileName", file.getOriginalFilename());
        response.put("size", String.valueOf(file.getSize()));

        return response;
    }
}
