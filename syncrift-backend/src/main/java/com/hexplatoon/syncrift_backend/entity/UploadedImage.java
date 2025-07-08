package com.hexplatoon.syncrift_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "uploaded_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name="uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if(uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }
}
