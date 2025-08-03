package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.UploadImageResponse;
import com.hexplatoon.syncrift_backend.entity.Image;
import com.hexplatoon.syncrift_backend.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public ResponseEntity<UploadImageResponse> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            Image savedImage = imageUploadService.uploadImage(imageFile); // Call the service method
            UploadImageResponse response = UploadImageResponse.builder() // Use builder pattern for DTO
                    .message("Image uploaded successfully to Cloudinary and URL saved to DB.")
                    .cloudinaryUrl(savedImage.getCloudinaryUrl())
                    .publicId(savedImage.getPublicId())
                    .imageId(savedImage.getId())
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("Upload failed (Bad Request): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UploadImageResponse.builder()
                    .message("Error: " + e.getMessage())
                    .build());
        } catch (IOException e) {
            System.err.println("Server error during file operation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UploadImageResponse.builder()
                    .message("Error: Failed to process image file.")
                    .build());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UploadImageResponse.builder()
                    .message("Error: An unexpected error occurred during image upload.")
                    .build());
        }
    }
}
