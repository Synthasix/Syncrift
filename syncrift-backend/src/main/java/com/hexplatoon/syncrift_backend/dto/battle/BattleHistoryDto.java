package com.hexplatoon.syncrift_backend.dto.battle;

import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.dto.user.MiniProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleHistoryDto {
    private Long id;
    private String category; // CSS, TB, CF
    private MiniProfileDto challenger;
    private MiniProfileDto opponent;
    private String status; // ONGOING, ENDED, etc.
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private Result resultJson;
    private Config configJson;
}
