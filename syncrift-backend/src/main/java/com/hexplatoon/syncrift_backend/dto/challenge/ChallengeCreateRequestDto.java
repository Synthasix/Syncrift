package com.hexplatoon.syncrift_backend.dto.challenge;

import com.hexplatoon.syncrift_backend.entity.Challenge.EventType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCreateRequestDto {

    // TODO : Rename challenge create dto
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Event type is required")
    private EventType eventType;
}

