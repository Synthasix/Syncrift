package com.hexplatoon.syncrift_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniProfileDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePicture;
}