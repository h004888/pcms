package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.response.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/uploads")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "UC14 - Admin Upload")
public class AdminUploadController {

    private final String uploadDir;

    public AdminUploadController(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @PostMapping
    @Operation(summary = "Upload banner/avatar image. Lưu local filesystem.")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "img";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0) ext = original.substring(dot);
        String stored = UUID.randomUUID() + ext;
        Path subdir = Paths.get(uploadDir, "home-banners");
        Files.createDirectories(subdir);
        Path target = subdir.resolve(stored);
        Files.write(target, file.getBytes());
        String url = "/api/v1/static/home-banners/" + stored;
        return ResponseEntity.ok(new UploadResponse(url, stored));
    }
}
