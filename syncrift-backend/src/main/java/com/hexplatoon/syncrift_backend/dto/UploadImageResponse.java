package com.hexplatoon.syncrift_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageResponse {
    private String message;
    private String cloudinaryUrl;
    private String publicId;
    private Long imageId;
}
