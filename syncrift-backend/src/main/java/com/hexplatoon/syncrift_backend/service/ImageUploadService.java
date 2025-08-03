package com.hexplatoon.syncrift_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hexplatoon.syncrift_backend.entity.Image;
import com.hexplatoon.syncrift_backend.repository.ImageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@Service
public class ImageUploadService {
    @Value("${image.upload.temp-dir}")
    private String tempDir;


    private Path tempUploadPath;

    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;

    public ImageUploadService(Cloudinary cloudinary, ImageRepository imageRepository) {
        this.cloudinary = cloudinary;
        this.imageRepository = imageRepository;
    }

    @PostConstruct
    public void init() {
        this.tempUploadPath = Paths.get(tempDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.tempUploadPath);
            System.out.println("Temporary upload directory created at: " + this.tempUploadPath);
        } catch (IOException e) {
            System.err.println("Could not create temp upload directory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public Image uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        File originalTempfile = null;
        File fileToUploadToCloudinary = null;

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            originalTempfile = this.tempUploadPath.resolve(uniqueFileName).toFile();
            file.transferTo(originalTempfile);

            // Process the image (resize, compress)
            fileToUploadToCloudinary = processImage(originalTempfile);

            // Extract dominant colors
            List<String> hexCodes = null;
            try {
                hexCodes = getDominantHexColors(originalTempfile, 4);
                System.out.println("Extracted Dominant Color Hex Codes: " + hexCodes);
            } catch (Exception e) {
                System.err.println("Failed to extract dominant color hex codes: " + e.getMessage());
                e.printStackTrace();
                // Set default empty list if color extraction fails
                hexCodes = new ArrayList<>();
            }
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(fileToUploadToCloudinary, ObjectUtils.asMap("resource_type", "auto"));
            System.out.println("Cloudinary Upload Result: " + uploadResult);

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            System.out.println("Uploaded to Cloudinary " + cloudinaryUrl + " with id " + publicId);

            Image image = Image.builder()
                    .cloudinaryUrl(cloudinaryUrl)
                    .publicId(publicId)
                    .colorCode(hexCodes)
                    .build();
            Image savedImage = imageRepository.save(image);
            return savedImage;
        } catch (IOException e) {
            System.err.println("Error during image upload: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("General error during image upload: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during image upload: " + e.getMessage(), e);
        } finally {
            // Clean up temporary files
            if (originalTempfile != null && originalTempfile.exists() && !originalTempfile.equals(fileToUploadToCloudinary)) {
                boolean deleted = originalTempfile.delete();
                if (deleted) {
                    System.out.println("Original temporary file deleted: " + originalTempfile.getName());
                } else {
                    System.err.println("Failed to delete original temporary file: " + originalTempfile.getName());
                }
            }

            if (fileToUploadToCloudinary != null && fileToUploadToCloudinary.exists()) {
                boolean deleted = fileToUploadToCloudinary.delete();
                if (deleted) {
                    System.out.println("Temporary file sent to Cloudinary deleted: " + fileToUploadToCloudinary.getName());
                } else {
                    System.err.println("Failed to delete temporary file sent to Cloudinary: " + fileToUploadToCloudinary.getName());
                }
            }
        }
    }

    /**
     * Validates the uploaded file to ensure it's a valid image
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
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
    /**
     * Analyzes an image to find the most dominant colors and returns their hex codes.
     * This method samples all pixels, counts their frequency, and returns a specified number of colors.
     * @param imageFile The image file to analyze.
     * @param maxColors The maximum number of dominant colors to return.
     * @return A list of hex color codes for the dominant colors.
     * @throws IOException If the image file cannot be read.
     */
    private List<String> getDominantHexColors(File imageFile, int maxColors) throws IOException {
        // Read the image into a BufferedImage object
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }

        Map<Integer, Integer> colorFrequency = new HashMap<>();

        // Iterate through all pixels and count color frequencies
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // Get the RGB value of the pixel
                int rgb = image.getRGB(x, y);
                // Increment the count for this color in the map
                colorFrequency.put(rgb, colorFrequency.getOrDefault(rgb, 0) + 1);
            }
        }

        // Create a list of map entries to sort them by frequency
        List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorFrequency.entrySet());

        // Sort the list in descending order based on the frequency (value)
        Collections.sort(sortedColors, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Extract the top 'maxColors' dominant colors and convert them to hex codes
        List<String> hexCodes = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : sortedColors) {
            if (count >= maxColors) { // Get only up to the specified number of colors
                break;
            }
            String hex = String.format("#%06X", (0xFFFFFF & entry.getKey()));
            hexCodes.add(hex);
            count++;
        }

        return hexCodes;
    }
}