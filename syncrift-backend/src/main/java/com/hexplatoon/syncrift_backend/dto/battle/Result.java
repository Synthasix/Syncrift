package com.hexplatoon.syncrift_backend.dto.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private String winnerUsername;
    private String loserUsername;
    private String winnerScore;
    private String loserScore;
}
