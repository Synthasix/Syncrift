package com.hexplatoon.syncrift_backend.dto.challenge;

import com.hexplatoon.rivalist_backend.entity.Challenge.ChallengeStatus;
import com.hexplatoon.rivalist_backend.entity.Challenge.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDto {
    private Long challengeId;
    private String senderUsername;
    private String recipientUsername;
    private ChallengeStatus status;
    private EventType eventType;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long timeRemainingSeconds; // Calculated field for frontend display
}

