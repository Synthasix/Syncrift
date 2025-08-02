package com.hexplatoon.syncrift_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hexplatoon.syncrift_backend.entity.UploadedImage;
import com.hexplatoon.syncrift_backend.repository.UploadedImageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {
    @Value("${image.upload.temp-dir}")
    private String tempDir;

    private Path tempUploadPath;

    private final Cloudinary cloudinary;
    private final UploadedImageRepository uploadedImageRepository;

    public ImageUploadService(Cloudinary cloudinary, UploadedImageRepository uploadedImageRepository) {
        this.cloudinary = cloudinary;
        this.uploadedImageRepository = uploadedImageRepository;
    }

    @PostConstruct
    public void init() {
        this.tempUploadPath = Paths.get(tempDir).toAbsolutePath().normalize();
        try{
            Files.createDirectories(this.tempUploadPath);
            System.out.println("Temporary upload directory created at: " + this.tempUploadPath); // Added for clarity
        }catch (IOException e){
            System.err.println("Could not create temp upload directory: " + e.getMessage()); // Changed to System.err
            e.printStackTrace();
            throw new RuntimeException("Could not create upload directory", e); // Pass the exception
        }
    }

    public UploadedImage uploadImage(MultipartFile file) throws IOException{
        if(file.isEmpty()){
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        File originalTempfile= null;
        File fileToUploadToCloudinary = null;

        try{
            String originalFilename = file.getOriginalFilename();
            String fileExtension = ""; // Corrected variable name
            if(originalFilename != null && originalFilename.contains(".")){
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")); // Corrected variable name
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            originalTempfile = this.tempUploadPath.resolve(uniqueFileName).toFile();
            file.transferTo(originalTempfile);

            // processImage will return either a NEW processed file, or originalTempfile if processing failed.
            fileToUploadToCloudinary = processImage(originalTempfile);

            // Using fileToUploadToCloudinary for upload
            Map uploadResult = cloudinary.uploader().upload(fileToUploadToCloudinary, ObjectUtils.asMap("resource_type","auto"));
            System.out.println("Cloudinary Upload Result: " + uploadResult); // Clarified output


            String cloudinaryUrl  = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            System.out.println("Uploaded to Cloudinary " + cloudinaryUrl + " with id " + publicId );

            UploadedImage uploadedImage = UploadedImage.builder()
                    .cloudinaryUrl(cloudinaryUrl)
                    .publicId(publicId)
                    .build();
            UploadedImage savedImage = uploadedImageRepository.save(uploadedImage);
            return savedImage;
        }catch (IOException e){
            System.err.println("Error during image upload: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }catch (Exception e){
            System.err.println("General error during image upload: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during image upload: " + e.getMessage(), e);
        }finally {
            // Delete the ORIGINAL temporary file only if a NEW processed file was created
            // (i.e., originalTempfile is different from fileToUploadToCloudinary)
            if (originalTempfile != null && originalTempfile.exists() && !originalTempfile.equals(fileToUploadToCloudinary)) {
                boolean deleted = originalTempfile.delete();
                if(deleted){
                    System.out.println("Original temporary file deleted: " + originalTempfile.getName() );
                }else{
                    System.err.println("Failed to delete original temporary file: " + originalTempfile.getName()); // Changed to System.err
                }
            }

            // Always delete the file that was actually used (or intended to be used) for Cloudinary upload
            // This handles both the case where processing created a new file and where it fell back to original.
            if(fileToUploadToCloudinary != null && fileToUploadToCloudinary.exists()){
                boolean deleted = fileToUploadToCloudinary.delete();
                if (deleted) {
                    System.out.println("Temporary file sent to Cloudinary deleted: " + fileToUploadToCloudinary.getName());
                } else {
                    System.err.println("Failed to delete temporary file sent to Cloudinary: " + fileToUploadToCloudinary.getName()); // Changed to System.err
                }
            }
        }
    }

    /**
     * Optional image processing (resize and compress) using Thumbnails library.
     * Returns the path to the processed file. If processing fails, returns the original file.
     * @param originalFile The temporarily saved file.
     * @return The processed file, or the original file if no processing occurred or failed.
     */
    private File processImage(File originalFile) throws IOException {
        String originalFileName = originalFile.getName();
        String baseName = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : originalFileName;
        String processedFileName = "processed_" + baseName + ".jpg";

        Path processedFilePath = this.tempUploadPath.resolve(processedFileName);
        File processedFile = processedFilePath.toFile();

        try {
            Thumbnails.of(originalFile)
                    .size(800, 800)
                    .outputFormat("jpg")
                    .outputQuality(0.80)
                    .toFile(processedFile);
            return processedFile;
        } catch (IOException e) {
            System.err.println("Image processing failed for " + originalFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return originalFile;
        }
    }

}