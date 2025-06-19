package com.hexplatoon.syncrift_backend.mapper;

import com.hexplatoon.syncrift_backend.dto.user.MiniProfileDto;
import com.hexplatoon.syncrift_backend.dto.user.ProfileDto;
import com.hexplatoon.syncrift_backend.entity.User;

public class ProfileMapper {
    public static ProfileDto toProfileDto(User user) {
        ProfileDto dto = ProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .level(user.getLevel())
                .experience(user.getExperience())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .typingRating(user.getTypingRating())
                .codeforcesRating(user.getCodeforcesRating())
                .cssDesignRating(user.getCssDesignRating())
                .codeforcesRating(user.getCodeforcesRating())
                .status(user.getStatus())
                .build();
        return dto;
    }
    public static MiniProfileDto toMiniProfileDto(User user){
        MiniProfileDto dto = MiniProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .build();
        return dto;
    }
}
