package com.hexplatoon.syncrift_backend.dto.battle;

import com.hexplatoon.syncrift_backend.dto.battle.progress.BattleProgress;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BattleSession {
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;
    private BattleProgress challengerProgress;
    private BattleProgress opponentProgress;
//    private Integer duration;
    // TODO : create functions for updating progress
}
