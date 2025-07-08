package com.hexplatoon.syncrift_backend.repository;

import com.hexplatoon.syncrift_backend.entity.UploadedImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedImageRepository extends JpaRepository<UploadedImage, Long> {
}
