package com.hexplatoon.syncrift_backend.repository;

import com.hexplatoon.syncrift_backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query(value = "SELECT * FROM images ORDER BY RANDOM() LIMIT 1", nativeQuery = true) // Use RANDOM() if PostgreSQL
    Image findRandomImage();
}
